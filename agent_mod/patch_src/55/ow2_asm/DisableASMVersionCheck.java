package moe.xinmu.asm_upgrade_patcher;

import moe.xinmu.minecraft_agent.Utils;
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;


public class DisableASMVersionCheck {
    public static boolean change(InstructionList il){
        boolean change=false;
        for (InstructionHandle i : il){
            Instruction ii=i.getInstruction();
            if(ii.getName().equals("bipush"))
                switch (((BIPUSH)ii).getValue().intValue()){
                    case 51:
                    case 52:
                    case 53:
                    case 54:
                    case 55:
                        i.setInstruction(new BIPUSH((byte)Utils.getJavaVersion()));
                        change=true;
                        break;
                }
        }
        return change;
    }
}
