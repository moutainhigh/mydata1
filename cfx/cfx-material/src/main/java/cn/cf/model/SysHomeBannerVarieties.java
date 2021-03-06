package cn.cf.model;

import java.util.Date;

/**
 *
 * @author 
 * @version 1.0
 * @since 1.0
 * */
public class SysHomeBannerVarieties  implements java.io.Serializable{
	private static final long serialVersionUID = 5454155825314635342L;
	
	
	private java.lang.String pk;
	private java.lang.String productnamePk;
	private java.lang.String varietiesName;
	private java.lang.String varietiesPk;
	private java.lang.Integer isVisable;
	private java.lang.Integer isDelete;
	private java.lang.Integer sort;
	private java.util.Date insertTime;
	private java.util.Date updateTime;
	//columns END

	public void setPk(java.lang.String pk) {
		this.pk = pk;
	}
	
	public java.lang.String getPk() {
		return this.pk;
	}
	
	public java.lang.String getProductnamePk() {
		return productnamePk;
	}

	public void setProductnamePk(java.lang.String productnamePk) {
		this.productnamePk = productnamePk;
	}

	public void setVarietiesName(java.lang.String varietiesName) {
		this.varietiesName = varietiesName;
	}
	
	public java.lang.String getVarietiesName() {
		return this.varietiesName;
	}
	public void setVarietiesPk(java.lang.String varietiesPk) {
		this.varietiesPk = varietiesPk;
	}
	
	public java.lang.String getVarietiesPk() {
		return this.varietiesPk;
	}
	public void setIsVisable(java.lang.Integer isVisable) {
		this.isVisable = isVisable;
	}
	
	public java.lang.Integer getIsVisable() {
		return this.isVisable;
	}
	public void setIsDelete(java.lang.Integer isDelete) {
		this.isDelete = isDelete;
	}
	
	public java.lang.Integer getIsDelete() {
		return this.isDelete;
	}
	public void setSort(java.lang.Integer sort) {
		this.sort = sort;
	}
	
	public java.lang.Integer getSort() {
		return this.sort;
	}
	public void setInsertTime(java.util.Date insertTime) {
		this.insertTime = insertTime;
	}
	
	public java.util.Date getInsertTime() {
		return this.insertTime;
	}
	public void setUpdateTime(java.util.Date updateTime) {
		this.updateTime = updateTime;
	}
	
	public java.util.Date getUpdateTime() {
		return this.updateTime;
	}
	

}