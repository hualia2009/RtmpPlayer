package com.sixnine.rtmpplayer;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Anchor implements Serializable {

	private static final long serialVersionUID = 1L;
	private String uid;// 用户id
	private String nickName; // 用户昵称
	private String weath;// 财富
	private String userType; 
	private String avatar;// 头像
	private String vipType; // VIP 类型
	private int platform; 
	private String credit; //用来判断主播等级 
	private String detail;//用户详细
	private String userNum;//用户靓号
	private String roomId; // 房间ID
	private String roomTag; 
	private String impress; //主播性格色彩
	private String audice; // 观众
	private String total;
	private String isPlay; 
	private String hostImage; // 主播头像
	private String hostAttId; 

	public String getHostAttId() {
		return hostAttId;
	}

	public void setHostAttId(String hostAttId) {
		this.hostAttId = hostAttId;
	}
	

	public String getHostImage() {
		return hostImage;
	}

	public void setHostImage(String hostImage) {
		this.hostImage = hostImage;
	}
	
	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getAudice() {
		return audice;
	}

	public void setAudice(String audice) {
		this.audice = audice;
	}

	public String getImpress() {
		return impress;
	}

	public void setImpress(String impress) {
		this.impress = impress;
	}
	
	public String getUserNum() {
		if(isInteger(userNum))
			return userNum;
		return "10000";
	}

	public void setUserNum(String userNum) {
		this.userNum = userNum;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public static boolean isInteger(String input){  
		if(input!=null){
	        Matcher mer = Pattern.compile("^[0-9]+$").matcher(input);  
	        return mer.find();
		}
		return false;
    }  
	
	public String getUid() {
		if(isInteger(uid))
			return uid;
		return "0";
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getWeath() {
		if(isInteger(weath))
			return weath;
		return "0";
	}

	public void setWeath(String weath) {
		this.weath = weath;
	}

	public String getUserType() {
		if(isInteger(userType))
			return userType;
		return "0";
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getVipType() {
		if(isInteger(vipType))
			return vipType;
		return "0";
	}

	public void setVipType(String vipType) {
		this.vipType = vipType;
	}

	public int getPlatform() {
		return platform;
	}

	public void setPlatform(int platform) {
		this.platform = platform;
	}

	public String getCredit() {
		if(isInteger(credit))
			return credit;
		return "0";
	}

	public void setCredit(String credit) {
		this.credit = credit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Anchor other = (Anchor) obj;
		if (uid == null) {
			if (other.uid != null)
				return false;
		} else if (!uid.equals(other.uid))
			return false;
		return true;
	}
	
	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public String getRoomTag() {
		return roomTag;
	}

	public void setRoomTag(String roomTag) {
		this.roomTag = roomTag;
	}
	
	public String getIsPlay() {
		return isPlay;
	}

	public void setIsPlay(String isPlay) {
		this.isPlay = isPlay;
	}

}
