package com.system.entity.character;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("gridDetector_info")
public class GridDetector implements Serializable {
    //Account Attributes
    @TableId
    private String id;
    private Integer cityId;

    //Personal Attributes
    private String idCard;
    private String name;
}
