package cn.cf.dao;


import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import cn.cf.dto.B2bBindDto;
import cn.cf.dto.B2bBindDtoEx;
import cn.cf.dto.B2bBindGoodsDto;
import cn.cf.model.B2bBind;

public interface B2bBindDaoEx extends B2bBindDao{
	
    List<B2bBindGoodsDto> getBindGoodsListByBpk(String bpk);
    
    List<B2bBindDtoEx> searchGridEx(Map<String, Object> map);

    int searchGridCount(Map<String, Object> map);

    int selectBoxes(@Param("bindPk") String bindPk);

    int selectSoldBoxes(@Param("bindPk") String bindPk);

    B2bBindDto getBind(String pk);
    
    int delBind(String pk);
    
    int updateEx(B2bBind model);
    
    int updateOverdue();

	int updateStatus(@Param("bindPk") String bindPk);

	List<B2bBindDto> getBindLists(Map<String, Object> map);
    
}
