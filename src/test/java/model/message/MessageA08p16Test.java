package model.message;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageA08p16Test {
    private String _1 = "1";
    private String _3 = "3";
    private String _4 = "4";
    private String _5 = "5";
    private String _6 = "6";
    private String _7 = "7";
    private String _8 = "8";
    private MessageA08p16 message = new MessageA08p16();

    @Before
    public void init(){
        message = new MessageA08p16();
        System.out.println("Init OK!");
    }

    @Test
    public void getNewCategory() {
        Assert.assertEquals(_1,message.getNewCategory("5"));
        Assert.assertEquals(_3,message.getNewCategory("7"));
        Assert.assertEquals(_4,message.getNewCategory("3"));
        Assert.assertEquals(_5,message.getNewCategory("11"));
        Assert.assertEquals(_6,message.getNewCategory("12"));
        Assert.assertEquals(_7,message.getNewCategory("13"));
        Assert.assertEquals(_8,message.getNewCategory("14"));
        Assert.assertEquals(" ",message.getNewCategory("bad"));
        Assert.assertNotNull(message.getNewCategory(null));
        System.out.println("good");
    }
}