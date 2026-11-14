package project.party;

/**
 * 太阳线注册VO
 */
public class SunLineReg extends NormalReg {

    private static final long serialVersionUID = 1188648056284724586L;

    /**
     * 推荐人usercode
     */
    private String reco_usercode;

	public String getReco_usercode() {
		return reco_usercode;
	}

	public void setReco_usercode(String reco_usercode) {
		this.reco_usercode = reco_usercode;
	}
}
