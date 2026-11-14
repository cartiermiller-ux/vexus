#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
db_password.py —— Vexus 交易所数据库密码加解密工具（纯 Python3，无第三方依赖）

算法与 Java 端 kernel.util.Endecrypt / kernel.druid.LocalDruidDataSource 完全一致：

    密钥   : MD5( KEY 的 GBK 字节 ) 16 字节，尾部补 8 个 0x00 凑成 24 字节
    明文   : 按 UTF-16LE 取字节
    加密   : DESede / ECB / PKCS5Padding（= PKCS7，块 8 字节）
    编码   : Base64 -> URLEncode(UTF-8 百分号编码)
    解密   : URLDecode -> Base64Decode -> DESede 解密 -> UTF-16LE 解码

固定 KEY 来自 kernel/druid/LocalDruidDataSource.java：
    Roj6#@08SDF87323FG00%jjsd

用法:
    python3 db_password.py decrypt "nY0NTjBcrC0nk6q9zp%2FJRCVu0GmCAI2kBNPDWppUOY3XuBTmq4d4gA%3D%3D"
    python3 db_password.py encrypt "MyStr0ng#Pass"
    python3 db_password.py selftest          # 用已知样例自测
    python3 db_password.py encrypt "密码" --key "其它KEY"

说明:
    * KEY 为 ASCII 时 GBK / UTF-8 字节相同；若换成含中文的 KEY，本脚本用 GBK 编码（与 Java 一致）。
    * Base64 使用标准字母表、无换行（Java 端 commons-codec Base64 默认不分块）。
"""

import base64
import hashlib
import sys

DEFAULT_KEY = "Roj6#@08SDF87323FG00%jjsd"

# 已知自测样例（dkewl.sql / config.properties 中现存密文）
KNOWN_CIPHER = "nY0NTjBcrC0nk6q9zp%2FJRCVu0GmCAI2kBNPDWppUOY3XuBTmq4d4gA%3D%3D"
KNOWN_PLAIN = "Err;2[eoGFUriwdgr"


# --------------------------------------------------------------------------
# 1. 纯 Python DES / 3DES 实现（ECB）
# --------------------------------------------------------------------------
_IP = [58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4,
       62, 54, 46, 38, 30, 22, 14, 6, 64, 56, 48, 40, 32, 24, 16, 8,
       57, 49, 41, 33, 25, 17, 9, 1, 59, 51, 43, 35, 27, 19, 11, 3,
       61, 53, 45, 37, 29, 21, 13, 5, 63, 55, 47, 39, 31, 23, 15, 7]
_FP = [40, 8, 48, 16, 56, 24, 64, 32, 39, 7, 47, 15, 55, 23, 63, 31,
       38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29,
       36, 4, 44, 12, 52, 20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27,
       34, 2, 42, 10, 50, 18, 58, 26, 33, 1, 41, 9, 49, 17, 57, 25]
_E = [32, 1, 2, 3, 4, 5, 4, 5, 6, 7, 8, 9, 8, 9, 10, 11, 12, 13,
      12, 13, 14, 15, 16, 17, 16, 17, 18, 19, 20, 21, 20, 21, 22, 23, 24, 25,
      24, 25, 26, 27, 28, 29, 28, 29, 30, 31, 32, 1]
_P = [16, 7, 20, 21, 29, 12, 28, 17, 1, 15, 23, 26, 5, 18, 31, 10,
      2, 8, 24, 14, 32, 27, 3, 9, 19, 13, 30, 6, 22, 11, 4, 25]
_PC1 = [57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18,
        10, 2, 59, 51, 43, 35, 27, 19, 11, 3, 60, 52, 44, 36,
        63, 55, 47, 39, 31, 23, 15, 7, 62, 54, 46, 38, 30, 22,
        14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 28, 20, 12, 4]
_PC2 = [14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10,
        23, 19, 12, 4, 26, 8, 16, 7, 27, 20, 13, 2,
        41, 52, 31, 37, 47, 55, 30, 40, 51, 45, 33, 48,
        44, 49, 39, 56, 34, 53, 46, 42, 50, 36, 29, 32]
_SHIFTS = [1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1]
_SBOX = [
    [14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7,
     0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8,
     4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0,
     15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13],
    [15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10,
     3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5,
     0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15,
     13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9],
    [10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8,
     13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1,
     13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7,
     1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12],
    [7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15,
     13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9,
     10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4,
     3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14],
    [2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9,
     14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6,
     4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14,
     11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3],
    [12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11,
     10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8,
     9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6,
     4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13],
    [4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1,
     13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6,
     1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2,
     6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12],
    [13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7,
     1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2,
     7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8,
     2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11],
]


def _permute(block, table, in_bits):
    out = 0
    for pos in table:
        out = (out << 1) | ((block >> (in_bits - pos)) & 1)
    return out


def _bytes_to_int(b):
    v = 0
    for x in b:
        v = (v << 8) | x
    return v


def _int_to_bytes(v, n):
    return bytes((v >> (8 * (n - 1 - i))) & 0xFF for i in range(n))


def _key_schedule(key8):
    k = _permute(_bytes_to_int(key8), _PC1, 64)          # 56 bit
    c = (k >> 28) & 0x0FFFFFFF
    d = k & 0x0FFFFFFF
    subkeys = []
    for shift in _SHIFTS:
        c = ((c << shift) | (c >> (28 - shift))) & 0x0FFFFFFF
        d = ((d << shift) | (d >> (28 - shift))) & 0x0FFFFFFF
        subkeys.append(_permute((c << 28) | d, _PC2, 56))
    return subkeys


def _feistel(r, subkey):
    x = _permute(r, _E, 32) ^ subkey                    # 48 bit
    out = 0
    for i in range(8):
        chunk = (x >> (42 - 6 * i)) & 0x3F
        row = ((chunk >> 4) & 0x2) | (chunk & 0x1)
        col = (chunk >> 1) & 0xF
        out = (out << 4) | _SBOX[i][row * 16 + col]
    return _permute(out, _P, 32)


def _des_block(block8, subkeys):
    v = _permute(_bytes_to_int(block8), _IP, 64)
    L = (v >> 32) & 0xFFFFFFFF
    R = v & 0xFFFFFFFF
    for sk in subkeys:
        L, R = R, L ^ _feistel(R, sk)
    return _int_to_bytes(_permute((R << 32) | L, _FP, 64), 8)


def des_encrypt_block(key8, block8):
    return _des_block(block8, _key_schedule(key8))


def des_decrypt_block(key8, block8):
    return _des_block(block8, list(reversed(_key_schedule(key8))))


def tdes_encrypt_block(key24, block8):
    """EDE: E(K1) D(K2) E(K3)"""
    b = des_encrypt_block(key24[0:8], block8)
    b = des_decrypt_block(key24[8:16], b)
    return des_encrypt_block(key24[16:24], b)


def tdes_decrypt_block(key24, block8):
    b = des_decrypt_block(key24[16:24], block8)
    b = des_encrypt_block(key24[8:16], b)
    return des_decrypt_block(key24[0:8], b)


def _pkcs7_pad(data):
    pad = 8 - (len(data) % 8)
    return data + bytes([pad]) * pad


def _pkcs7_unpad(data):
    if not data or len(data) % 8:
        raise ValueError("密文长度不是 8 的整数倍，密文可能损坏")
    pad = data[-1]
    if pad < 1 or pad > 8:
        raise ValueError("PKCS#5/7 填充非法（密钥不对或密文损坏）")
    return data[:-pad]


def des3_encrypt(data, key24):
    data = _pkcs7_pad(data)
    out = bytearray()
    for i in range(0, len(data), 8):
        out += tdes_encrypt_block(key24, data[i:i + 8])
    return bytes(out)


def des3_decrypt(data, key24):
    out = bytearray()
    for i in range(0, len(data), 8):
        out += tdes_decrypt_block(key24, data[i:i + 8])
    return _pkcs7_unpad(bytes(out))


# --------------------------------------------------------------------------
# 2. 业务层：KEY 派生 / URL 编码 / 对外加解密
# --------------------------------------------------------------------------
def derive_key(key_str=DEFAULT_KEY):
    """MD5(KEY 的 GBK 字节) 取 16 字节，补 0 到 24 字节"""
    try:
        raw = key_str.encode("gbk")
    except (UnicodeEncodeError, LookupError):
        raw = key_str.encode("utf-8")
    digest = hashlib.md5(raw).digest()
    return digest + b"\x00" * 8


def url_encode(s):
    """等价 Java URLEncoder.encode(s, "UTF-8")：保留 A-Za-z0-9 -_.* ，空格变 +"""
    safe = b"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.*"
    out = []
    for byte in s.encode("utf-8"):
        ch = bytes([byte])
        if byte == 0x20:
            out.append("+")
        elif ch in [bytes([c]) for c in safe]:
            out.append(chr(byte))
        else:
            out.append("%%%02X" % byte)
    return "".join(out)


def url_decode(s):
    s = s.replace("+", " ")
    out = bytearray()
    i = 0
    while i < len(s):
        if s[i] == "%" and i + 2 < len(s) + 1 and i + 2 <= len(s) - 1 + 1:
            try:
                out.append(int(s[i + 1:i + 3], 16))
                i += 3
                continue
            except ValueError:
                pass
        out += s[i].encode("utf-8")
        i += 1
    return out.decode("utf-8", errors="replace")


def encrypt(plaintext, key_str=DEFAULT_KEY):
    key24 = derive_key(key_str)
    cipher = des3_encrypt(plaintext.encode("utf-16-le"), key24)
    return url_encode(base64.b64encode(cipher).decode("ascii"))


def decrypt(ciphertext, key_str=DEFAULT_KEY):
    key24 = derive_key(key_str)
    b64 = url_decode(ciphertext.strip())
    raw = base64.b64decode(b64)
    plain = des3_decrypt(raw, key24)
    return plain.decode("utf-16-le", errors="replace")


# --------------------------------------------------------------------------
# 3. CLI
# --------------------------------------------------------------------------
def selftest():
    ok = True
    # (a) 标准 DES 测试向量：key=133457799BBCDFF1 pt=0123456789ABCDEF -> 85E813540F0AB405
    des_ct = des_encrypt_block(bytes.fromhex("133457799BBCDFF1"),
                               bytes.fromhex("0123456789ABCDEF")).hex().upper()
    print("[1] DES 标准向量      : %s  %s" % (des_ct, "OK" if des_ct == "85E813540F0AB405" else "FAIL"))
    ok &= des_ct == "85E813540F0AB405"

    # (b) 已知密文解密
    try:
        pt = decrypt(KNOWN_CIPHER)
        good = pt == KNOWN_PLAIN
        print("[2] 已知密文解密      : %r  %s" % (pt, "OK" if good else "FAIL(期望 %r)" % KNOWN_PLAIN))
        ok &= good
    except Exception as exc:  # noqa: BLE001
        print("[2] 已知密文解密      : 异常 %s" % exc)
        ok = False

    # (c) 往返
    pt2 = "Test@123456 中文密码"
    ct2 = encrypt(pt2)
    rt = decrypt(ct2)
    print("[3] 加解密往返        : %r -> %s -> %r  %s" % (pt2, ct2, rt, "OK" if rt == pt2 else "FAIL"))
    ok &= rt == pt2

    # (d) 加密已知明文应得到已知密文（密钥/算法完全一致的强校验）
    ct_known = encrypt(KNOWN_PLAIN)
    good = ct_known == KNOWN_CIPHER
    print("[4] 已知明文再加密    : %s  %s" % (ct_known, "OK" if good else "FAIL(期望 %s)" % KNOWN_CIPHER))
    ok &= good
    print("selftest:", "PASS" if ok else "FAIL")
    return 0 if ok else 1


def usage():
    print(__doc__)
    print("示例:")
    print("  python3 db_password.py decrypt \"nY0NTjBcrC0nk6q9zp%%2FJRCVu0GmCAI2kBNPDWppUOY3XuBTmq4d4gA%%3D%%3D\"")
    print("  python3 db_password.py encrypt \"MyStr0ng#Pass\"")


def main(argv):
    if len(argv) < 2:
        usage()
        return 2
    action = argv[1].lower()
    key = DEFAULT_KEY
    rest = argv[2:]
    if "--key" in rest:
        i = rest.index("--key")
        key = rest[i + 1]
        rest = rest[:i] + rest[i + 2:]
    if action in ("-h", "--help", "help"):
        usage()
        return 0
    if action == "selftest":
        return selftest()
    if action == "encrypt":
        if not rest:
            usage()
            return 2
        print(encrypt(rest[0], key))
        return 0
    if action == "decrypt":
        if not rest:
            usage()
            return 2
        try:
            print(decrypt(rest[0], key))
        except Exception as exc:  # noqa: BLE001
            print("解密失败: %s" % exc, file=sys.stderr)
            return 1
        return 0
    usage()
    return 2


if __name__ == "__main__":
    sys.exit(main(sys.argv))
