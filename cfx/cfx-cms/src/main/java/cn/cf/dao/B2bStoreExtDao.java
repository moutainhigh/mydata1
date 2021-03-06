/*
 * Powered By [9client]
 * Web Site: http://www.9client.com
 * Since 2012 - 2018
 */

package cn.cf.dao;

import java.util.List;
import java.util.Map;

import cn.cf.dto.B2bStoreDto;
import cn.cf.dto.B2bStoreExtDto;

/**
 * 
 * @author
 * @version 1.0
 * @since 1.0
 * */
public interface B2bStoreExtDao extends B2bStoreDao {

	List<B2bStoreExtDto> getByCompanyPkExt(String companyPk);

	List<B2bStoreExtDto> searchGridExt(Map<String, Object> map);

	int searchGridExtCount(Map<String, Object> map);
	
	int updateStore(B2bStoreExtDto dto);

    List<B2bStoreExtDto> getMap(Map<String, Object> map);

    List<B2bStoreDto> getByCompanyBlock(Map<String, Object> map);

}
