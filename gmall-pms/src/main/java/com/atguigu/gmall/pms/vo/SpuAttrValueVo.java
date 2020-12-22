package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;


public class SpuAttrValueVo extends SpuAttrValueEntity {

    public void setValueSelected(List<String> valueSelected){
        String join = StringUtils.join(valueSelected, ",");
        if (CollectionUtils.isEmpty(valueSelected)){
            return;
        }
        this.setAttrValue(join);
    }
}
