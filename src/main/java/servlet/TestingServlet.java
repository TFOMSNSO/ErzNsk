package servlet;


import answer.AnswerData;
import answer.AnswerZp;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import help.Const;
import loadparse.Load;
import loadparse.ZpLoadMock;
import model.message.*;
import model.other.ListWebForXMLQuery;
import org.apache.catalina.websocket.WsOutbound;
import services.Services;
import task.Task;
import task.TaskMock;
import util.ConstantiNastrojki;
import util.UtilParseDbXml;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@ServerEndpoint("/wsanswernew")
public class TestingServlet  {
    private static final long serialVersionUID = 1L;
    List<String> listKluchi = new ArrayList<>();
    String userMachine;
    private Message messageZp1;
    private Task task;
    private HttpServletRequest request;
    WsOutbound myoutbound;
    Services services = new Services();


    @OnOpen
    public void onOpen(){
        System.out.println("Open Connection ...");
    }

    @OnClose
    public void onClose(){
        System.out.println("Close Connection ...");
    }

    @OnError
    public void onError(Throwable e){
        e.printStackTrace();
    }



}
