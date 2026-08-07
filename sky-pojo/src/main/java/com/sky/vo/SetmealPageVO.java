package com.sky.vo;

import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetmealPageVO extends Setmeal {
    private String categoryName;
    private List<SetmealDish> setmealDishes;
}
