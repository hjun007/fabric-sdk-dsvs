package com.bjca.fabricsdkdsvsrest.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TxArgsPojo {
    private String chaincodeName;
    private String function;
    private String[] args;
}
