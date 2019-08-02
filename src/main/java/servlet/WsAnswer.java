package servlet;

import help.Const;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import loadparse.Load;
import loadparse.ZpLoadMock;
import model.message.Message;
import model.message.MessageA03p07;
import model.message.MessageA08p01;
import model.message.MessageA08p02;
import model.message.MessageA08p03;
import model.message.MessageA08p03For;
import model.message.MessageA08p03kol;
import model.message.MessageA08p03pr;
import model.message.MessageA08p04;
import model.message.MessageA08p06;
import model.message.MessageA08p08;
import model.message.MessageA08p14;
import model.message.MessageA08p16;
import model.message.MessageA08v01;
import model.message.MessageA13p09;
import model.message.MessageA24p10;
import model.message.MessageA24p102;
import model.message.MessageP26;
import model.message.MessageP27;
import model.message.MessageZp1;
import model.message.MessageZp1Fiod;
import model.message.MessageZp1pr;
import model.message.MessageZp3;
import model.message.MessageZp9;
import model.other.ListWebForXMLQuery;
import services.Services;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import answer.AnswerData;
import answer.AnswerZp;
import task.Task;
import task.TaskMock;
import util.ConstantiNastrojki;
import util.UtilParseDbXml;



public class WsAnswer extends WebSocketServlet {
	private static final long serialVersionUID = 1L;


	@Override
	protected StreamInbound createWebSocketInbound(String arg0,HttpServletRequest request) {
		System.out.println("creating socket");
		return new SocketMess(request);
	}

	private class SocketMess extends MessageInbound {

		// ��������� ��� ZP1, ���������� ZP1 � 0 ��� 1 ��� ������ ��� ��� ������
		// �����
		List<String> listKluchi = new ArrayList<String>();
		/*
		 * ��� �������� � ���������� index.jsp �������� ���������� �� ������ �
		 * ����� �� ���������� �� ������ ��� ������������ � ������ USER
		 * (USERPonomarev) ��� ��������� � ������������ � ������. ����� ��������
		 * ��� �� �������.
		 */
		String userMachine;
		private Message messageZp1;
		private Task task;
		private HttpServletRequest request;
		WsOutbound myoutbound;
		Services services = new Services();
		private SocketMess(HttpServletRequest request){
			this.request= request;
		}

		@Override
		protected void onBinaryMessage(ByteBuffer arg0) throws IOException {
		}


		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.apache.catalina.websocket.MessageInbound#onTextMessage(java.nio
		 * .CharBuffer) �������� ��� ������ index.jsp ����������� ������
		 * webSocketAnswer � ���������� ���������� �� ������ ��� ������ �������
		 * �� ������� ���� ������������ �� ������ ��� ������������ ��� �������
		 * �� zp1 ������������ ��� ��������� zp1. 2 ����� �������� � ��������� � �����������
		 * �� ��� �� ��� ������ � �������.
		 */
		protected void onTextMessage(CharBuffer arg0) throws IOException
		{
			System.out.println("erznsk_1: onTextMessage arg0:" + arg0);


			if(arg0.toString().contains("USERENTERINTOSYSTEM") || arg0.toString().contains("xmlotladkazaprosa"))
			{
				// ������������� ��������� ��� ������� �� ������� ����
				userMachine = initUsername(arg0);
				// ���� ������� (��������� ����������� xml �������� � �����
				// output)
				flagOtladkiXML(arg0);
			} else//?????? ??? ?? ??? ??? switch, ? ?? ??? ??? ???
				{
					// ����� ���������� ������ ��� zp1
				if (arg0.toString().equals("ZP1")|| arg0.toString().equals("0"))
				{ System.out.println("��������� � ZP1 � ���������  "+ arg0.toString()); parseEnterMesForZP1(arg0);}
					else
					{
						if(arg0.toString().equals("Zp1Ajax"))
						{
							listKluchi.add(arg0.toString());
							System.out.println("first part of zp1ajax "+ listKluchi);
						}
						else if(arg0.toString().equals("Zp1taskA8P4")){
							listKluchi.add(arg0.toString());
							System.out.println("����� �� ������ ����� ������ ����� ������� Zp1taskA8P4"+ listKluchi);
						}
						else if(arg0.toString().length() >= 11 && arg0.toString().substring(2, 13).equals("Zp1taskA8P4"))
						{
							listKluchi.add(arg0.toString());
							System.out.println("����� �� ������ ����� ������ ����� ������� Zp1taskA8P4 ");
						}
						else
						{
							if(arg0.toString().length() >= 11 && arg0.toString().substring(2, 9).equals("Zp1Ajax"))
							{
									listKluchi.add(arg0.toString());
									System.out.println("����� �� ������ ����� ������ ����� ������� zp1ajax ");
							}
							else
							{
								if(arg0.toString().equals("A08P02test"))
								{
									listKluchi.add(arg0.toString());
									System.out.println("����� �� ������ ����� ������ ����� ������� A08P02test "+ listKluchi);
								}
								else
								{
									if(arg0.toString().length() >= 10 && arg0.toString().substring(2, 7).equals("list1"))
									{
											listKluchi.add(arg0.toString());
											System.out.println("����� �� ������ ����� ������ ����� ������� A08P02test "+ listKluchi);
									}
									else
									{
										if(arg0.toString().equals("buttonZP9"))
										{
											listKluchi.add(arg0.toString());
											System.out.println("����� �� ������ ����� ������ ����� ������� ZP9test "+ listKluchi);
										}
										else
										{
											if(arg0.toString().length() >= 10 && (	arg0.toString().contains("list1enpzp9") ||	arg0.toString().contains("list1passportzp9") ||	arg0.toString().contains("list1snilszp9")	)	)
											{
													listKluchi.add(arg0.toString());
													System.out.println("����� �� ������ ����� ������ ����� ������� ZP9test "+ listKluchi);
											}
											else
											{
												if(arg0.toString().equals("A08P14"))
												{
													listKluchi.add(arg0.toString());
													System.out.println("����� �� ������ ����� ������ ����� ������� A08P14 "+ listKluchi);
												}
												else
												{
													if(arg0.toString().length() >= 10 && arg0.toString().contains("A08P14"))
													{
															listKluchi.add(arg0.toString());
															System.out.println("����� �� ������ ����� ������ ����� ������� A08P14 "+ listKluchi);
													}
													else
													{
														if(arg0.toString().equals("ZP3")){

															listKluchi.add(arg0.toString());
															System.out.println("����� �� ������ ����� ������ ����� ������� ZP3 "+ listKluchi);
														}
														else{

															if(arg0.toString().length() >= 10 && arg0.toString().contains("ZP3secondpart"))
															{
																	listKluchi.add(arg0.toString());
																	System.out.println("����� �� ������ ����� ������ ����� ������� ZP3 "+ listKluchi);
															}else{

																	if(arg0.toString().equals("A03P07"))
																	{
																		listKluchi.add(arg0.toString());
																		System.out.println("����� �� ������ ����� ������ ����� ������� A03P07 "+ listKluchi);
																	}else
																	{
																		if(arg0.toString().length() >= 10 && arg0.toString().contains("A03P07"))
																		{
																				listKluchi.add(arg0.toString());
																				System.out.println("����� �� ������ ����� ������ ����� ������� A03P07 "+ listKluchi);
																		}
																		else{
																			if(arg0.toString().equals("A08P08"))
																			{
																				listKluchi.add(arg0.toString());
																				System.out.println("����� �� ������ ����� ������ ����� ������� A08P08 "+ listKluchi);
																			}
																			else{
																				if(listKluchi.size() == 1 && listKluchi.get(0).equals("A08P08"))
																				{
																						listKluchi.add(arg0.toString());
																						System.out.println("����� �� ������ ����� ������ ����� ������� A08P08 "+ listKluchi);
																				}
																				else
																				{
																					// ?????????? ??? ?????????
																					listKluchi.add(arg0.toString());
																					listKluchi.add("ostalnizaprosi");
																					System.out.println("����� �� ������ ����� ������ ����� ������� "+ listKluchi);
																				}
																			}
																		}
																	}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				// for debug
				 //System.out.println("posle if else listklusch:" + listKluchi);


				// ?????????? ????? ?? 2 ????? : zp1 ? 1 ??? 0
				if (listKluchi.size() == 2 && listKluchi.get(0).equals("ZP1") && (listKluchi.get(1).equals("1") || listKluchi.get(1).equals("0")))
				{
					//System.out.println("IF------arg0:" + arg0);
					//ZP1 ����
					methoZp1(myoutbound);
					// ??????? ????????? ??????
					listKluchi.clear();
				} else
				{
					System.out.println("size:" + listKluchi.size() + " |list:" + listKluchi);
					// ???? ?? ?? ? ?????? ?????? ???? ...
					if (listKluchi.size() == 2 && !listKluchi.get(0).equals("ZP1"))
					{

						// ????? ?????? ????????? (??? ????) ??? zp1ajax

							if(listKluchi.get(1).substring(2, 9).equals("Zp1Ajax"))
							{

								System.out.println("lovim ajax ");
								try {
										methoZp1(request,myoutbound,listKluchi.get(1),0);
								} catch (JAXBException e) {
										e.printStackTrace();
								}
								// ??????? ????????? ??????
								listKluchi.clear();
							}
							else if(listKluchi.get(1).substring(2, 13).equals("Zp1taskA8P4"))
							{
								System.out.println("����� ZP1 ��� A08P04");
								try {
										methoZp1(request,myoutbound,listKluchi.get(1),1);
								} catch (JAXBException e) {
										e.printStackTrace();
								}
								// ??????? ????????? ??????
								listKluchi.clear();
							}
							else
							{
										try
										{
											System.out.println("������ �����");
											lovim_ostalnieZaprosi(myoutbound, listKluchi.get(0),listKluchi.get(1));
											// ??????? ????????? ??????
											listKluchi.clear();
										} catch (Exception e) {
											e.printStackTrace();
										}
							}



					}

				}
		}

		public void onOpen(WsOutbound outbound) {
			System.out.println("Open Client websocket");
			this.myoutbound = outbound;
		}

		public void onClose(int status) {
			System.out.println("Close Server websocket");

		}

		/*
		 * ??? ???????? ? ?????????? index.jsp ???????? ?????????? ?? ?????? ?
		 * ????? ?? ?????????? ?? ?????? ??? ???????????? ? ?????? USERENTERINTOSYSTEM
		 * (USERENTERINTOSYSTEMPonomarev) ??? ????????? ? ???????????? ? ??????. ????? ????????
		 * ??? ?? ???????.
		 */
		private String initUsername(CharBuffer arg0) {
			String vr = null;
			if (arg0.toString().contains("USERENTERINTOSYSTEM")) {
				vr = arg0.toString().substring(19);
			}

			return vr;
		}

		/*
		 * ????? ?? ??????? ???????? ???? xmlotladkazaprosa1 ????
		 * xmlotladkazaprosa0 ??????????? ???? xmlotladkazaprosa ? ?????????
		 * ???????? ?? ????? ?????
		 */
		private void flagOtladkiXML(CharBuffer arg0) {
			if (arg0.toString().substring(0, 17)
					.equalsIgnoreCase("xmlotladkazaprosa")) {
				ConstantiNastrojki.otladkaXML = arg0.toString().substring(17);
				System.out.println("ConstantiNastrojki.otladkaXML= "
						+ ConstantiNastrojki.otladkaXML);
			}
		}

		/*
		 * ????? ?????????? ?????? ??? ??????????? ??? ????? zp1
		 */
		private void parseEnterMesForZP1(CharBuffer arg0) {

			if (arg0.toString().equals("0")) {
				System.out.println("������� 0");
				listKluchi.add(arg0.toString());
			}
			if (arg0.toString().equals("1")) {
				System.out.println("������� 1");
				listKluchi.add(arg0.toString());
			}
			if (arg0.toString().equals("ZP1")) {
				System.out.println("������� zp1");
				listKluchi.add(arg0.toString());
			}
			System.out.println("listKluchi " + listKluchi.size());
		}

		private void methoZp1(WsOutbound myoutbound) throws IOException {

			messageZp1 = new MessageZp1();
			task = new TaskMock();

			try {
				if (task.add(userMachine)) {
					if (messageZp1.create(userMachine)) {
						new AnswerData().loadToExcel(messageZp1.getDataList(),userMachine + ".xls");
						String file = "50000-" + messageZp1.getGuidBhs() + ".uprmes";
						String fileUpr2 = "50000-" + messageZp1.getGuidBhs();
						String sentMessages = "";
						File to_file = new File(Const.OUTPUTDONE + fileUpr2+ ".uprak2");

						CharBuffer buffer3 = CharBuffer.wrap("|��������� "
								+ file);
						myoutbound.writeTextMessage(buffer3);

						while ("".equals(sentMessages)) {
							try {
								Thread.sleep(60000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							if (to_file.exists()) {
								sentMessages = "> ������� " + to_file.toString();
								CharBuffer buffer5 = CharBuffer.wrap(sentMessages);
								// ??????? ???????? ??????
								if (ConstantiNastrojki.openWorkbookXls == 1) myoutbound.writeTextMessage(buffer5);
							} else
							{
								CharBuffer buffer6 = CharBuffer.wrap("> ������� ������  uprak2");
								myoutbound.writeTextMessage(buffer6);
							}
						}

						Load zpLoad = new ZpLoadMock();
						if (zpLoad.load(fileUpr2)) {
							String excelFile = userMachine + ".xls";
							File excelFileEssence = new File("\\\\asu-srp\\ErzNsk_all_the_rest\\excel\\" + excelFile);
							if (excelFileEssence.exists()) {
								new AnswerZp().loadToExcel(userMachine);
								CharBuffer buffer4 = CharBuffer
										.wrap("> ��������� � ������");
								myoutbound.writeTextMessage(buffer4);
								System.out.println("���� ������ "
										+ excelFileEssence);
								// ???? ???? ?????? 1 ??? 0
								if (listKluchi.get(1).equals("1")) {
									if (Desktop.isDesktopSupported()) {
										Desktop.getDesktop().open(
												excelFileEssence);
									}

								}

							} else {
								System.out
										.println("�������� � ������ �� ���������");
							}

						} else {
							System.out.println("������ � �������� ���2");
						}

					} else {
						System.out.println("������ create");
					}
				} else {
					System.out.println("������ add");
				}
			} catch (Exception e) {
				CharBuffer err = CharBuffer
						.wrap("> �������� ������, ��������� ������ �� ������ ��� ��");
				myoutbound.writeTextMessage(err);
				onClose(0);
				e.printStackTrace();
			}

		}

		private void methoZp1(HttpServletRequest request,WsOutbound myoutbound,String stList, int kl) throws IOException, JAXBException {
			 int pERSON_SURNAME = 0,pERSON_KINDFIRSTNAME = 1,pERSON_KINDLASTNAME = 2,pERSON_BIRTHDAY = 3, pERSON_ADDRESSID= 4,  PERSON_SERPOLICY = 5, PERSON_NUMPOLICY = 6,pERSON_SEX=7,pERSON_SERDOC = 8,pERSON_NUMDOC = 9,PERSON_REGNUMBER = 10,pERSON_LINKSMOESTABLISHMENTID = 11,PERSON_ESTABLISHMENTAMBUL = 12,PERSON_DATECHANGE = 13, PERSON_ESTABLISHMENTDENT = 14,PERSON_SOCIALID = 15,PERSON_STATUSID = 16,pERSON_DOCPERSONID = 17,PERSON_INSPECTION = 18,PERSON_OPERATION = 19,PERSON_STATUSREC = 20,PERSON_OUTID = 21,PERSON_INSPECTORID = 22,PERSON_ESTABLISHMENTID = 23,PERSON_DATEPOLICY = 24,pERSON_DATEINPUT = 25,eNP = 26,SMO_OLD = 27,PERSONADD_ADDRESSID = 28,PERSONADD_PRIM = 29,sNILS = 30,bORN = 31,dATEPASSPORT = 32,rUSSIAN = 33,TELEDOM = 34,TELEWORK = 35,EMAIL = 36,TELE2 = 37,DOK_VI = 38,eNP_PA= 39,ZA = 40,zAD= 41,ZAP = 42,PRED = 43,d_V= 44,d_SER= 45,d_NUM= 46,D_DATE = 47,METHOD = 48,PETITION = 49,FPOLIC = 50,pR_FAM = 51,pR_IM= 52,pR_OT= 53,PR_TEL = 54,PR_ADRES = 55,vS_NUM= 56,vS_DATE= 57,D1 = 58,d2=59,ENP_DATE = 60,lAST_FAM =61,lAST_IM= 62,lAST_OT= 63,lAST_DR= 64,KATEG = 65,DATE_PRIK = 66,MSA =67;
			 int d_13= 777, eNP_1= 777,eNP_2= 777, p14cx1= 777,p14cx5= 777,p14cx6= 777,p14cx7= 777,uSER_PERSON_SURNAME= 777,xPN1= 777,xPN2 = 777,xPN3= 777,uSERNAME= 777,zADMINUS1= 777,zADPLUS40= 777,nBLANC= 777,vS_DATEPLUS1= 777,uSER_ENP= 777,uSER_PERSON_KINDFIRSTNAME= 777,uSER_PERSON_KINDLASTNAME= 777,uSER_SMO= 777,uSER_D_12= 777, d_12_PLUS1= 777,uSER_DOC_DATE= 777,uSER_DOCID= 777,uSER_NUMDOC= 777,uSER_SERDOC= 777,pFR_NOTID= 777,pFR_ID= 777, pFR_SNILS= 777,uSER_POL= 777,uSER_D_13= 777,uSER_OKATO_3= 777,uSER_TYPE_POL= 777,oKATO_3 = 777, tYPE_POL = 777,d_12 = 777,pOL = 777;



			 UtilParseDbXml utilparsedbXml = new UtilParseDbXml();



			 messageZp1 = new MessageZp1(pERSON_SERDOC, pERSON_NUMDOC, pERSON_DOCPERSONID, pERSON_SURNAME, pERSON_KINDFIRSTNAME, pERSON_KINDLASTNAME, pERSON_BIRTHDAY, pERSON_SEX, pERSON_LINKSMOESTABLISHMENTID, eNP, pERSON_ADDRESSID, pERSON_DATEINPUT, sNILS, bORN, dATEPASSPORT, eNP_PA, vS_NUM, vS_DATE, zAD, d2, pERSON_LINKSMOESTABLISHMENTID, d_12, d_13, oKATO_3, tYPE_POL, pOL, eNP_1, eNP_2, p14cx1, p14cx5, p14cx6, p14cx7, xPN1, xPN2, xPN3, uSERNAME, zADMINUS1, zADPLUS40, nBLANC, vS_DATEPLUS1, uSER_ENP, uSER_PERSON_SURNAME, uSER_PERSON_KINDFIRSTNAME, uSER_PERSON_KINDLASTNAME, uSER_SMO, uSER_D_12, uSER_D_13, uSER_OKATO_3, uSER_TYPE_POL, uSER_POL, rUSSIAN, d_V, d_SER, d_NUM, pR_FAM, pR_IM, pR_OT, lAST_FAM, lAST_IM, lAST_OT, lAST_DR, pFR_SNILS, pFR_ID, pFR_NOTID, uSER_SERDOC, uSER_NUMDOC, uSER_DOCID, uSER_DOC_DATE, d_12_PLUS1);
			//	messageZp1 = new MessageZp1();
				task = new TaskMock();
			/* ���� �������� �������� 0 �� ������������ ������ �������� ������� ���� ������(� ������ ������� �� ������ ��� ���������) �� ������� ������ ������������
			 * if kl ==0 zp1ajax else if kl ==1 zp1 for a08p04 on fiod
			 */
			if (messageZp1.create(userMachine,stList,kl))
			{
			    System.out.println("create ok");
				String file = "50000-" + messageZp1.getGuidBhs()+ ".uprmes";
				String fileUpr2 = "50000-" + messageZp1.getGuidBhs();
				String sentMessages = "";
				File to_file = new File(Const.OUTPUTDONE + fileUpr2+ ".uprak2");
				utilparsedbXml.marshaling("50000-" + messageZp1.getGuidBhs(),messageZp1.getDataList());

				CharBuffer buffer3 = CharBuffer.wrap("��������� "+ file);
				myoutbound.writeTextMessage(buffer3);

				while ("".equals(sentMessages)) {
					try {
						Thread.sleep(15000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//???????? ??? ????????? ?? ??????? ???????? ???????? ?????? ?? ?????????? ???????? ?????
					if (to_file.exists())
					{
						//System.out.println(to_file.getAbsolutePath() + "      :" + "exists");
						// ?????????? ?? ??????? ???????? ??????2
						sentMessages = "> ������� " + fileUpr2+".uprak2"+" zp1";
						CharBuffer buffer5 = CharBuffer.wrap(sentMessages);
						     /*
							 * ????????  ? wbAnswer.js json ?????? Uprmes'a (????????? ??????? ????????? uprmess)
							 * ?? ?????? ???? ?????? ??????????? ?????? ???? web ??????
							 *
							 * ?? ????? ????? .fromdbforuprmess, ????? ?????? ?? ?????????? ??????? ?????? ???????? ????? ? ?????? ??? ??? ????????
							 */
						// ?????????? ? ????? ???? ????? ??????? ?? ??????? ? ?????????? ??????
						CharBuffer buffer777 = CharBuffer.wrap("50000-" + messageZp1.getGuidBhs()+".fromdbforuprmess");
						// ??????? ?? ????? wbAnswer.js  c ??????? ??? ??????? (?????? ??????? ?? ????)
						myoutbound.writeTextMessage(buffer777);
						// ?????????? ????????? ??? ??????? ?????2
						myoutbound.writeTextMessage(buffer5);
					} else
					{
						CharBuffer buffer6 = CharBuffer
								.wrap("> ������� ������ uprak2");
						myoutbound.writeTextMessage(buffer6);
					}
				}
			}
		}

		private void lovim_ostalnieZaprosi(WsOutbound myoutbound,String vidZaprosa, String jsonString) throws Exception
        {
			 int sMO = 4,d_12 = 5,pERSON_DATEINPUT = 6,oKATO_3 =7,pERSON_SURNAME = 1,pERSON_KINDFIRSTNAME = 2,pERSON_KINDLASTNAME = 3,pERSON_BIRTHDAY = 21,tYPE_POL = 8,
       			 pOL = 9,bORN = 18,eNP = 0,pERSON_SERDOC = 13,pERSON_NUMDOC = 14, pERSON_DOCPERSONID = 20,dATEPASSPORT = 15,d2=17,pERSON_SEX=16, rUSSIAN = 19,pid29 = 777;
			int pERSON_LINKSMOESTABLISHMENTID= 0;
			int pERSON_ADDRESSID= 0;
			int sNILS = 0;
			int eNP_PA= 0;
			int vS_NUM= 0;
			int vS_DATE= 0;
			int zAD= 0;
			int d_13= 0;
			int eNP_1= 0;
			int eNP_2= 0;
			int p14cx1= 0;
			int p14cx5= 0;
			int p14cx6= 0;
			int p14cx7= 0;
			int d_SER= 0;
			int d_V= 0;
			int d_NUM= 0;
			int uSER_PERSON_SURNAME= 0;
			int xPN1= 0;
			int xPN2 = 0;
			int xPN3= 0;
			int uSERNAME= 0;
			int zADMINUS1= 0;
			int zADPLUS40= 0;
			int nBLANC= 0;
			int vS_DATEPLUS1= 0;
			int uSER_ENP= 0;
			int uSER_PERSON_KINDFIRSTNAME= 0;
			int uSER_PERSON_KINDLASTNAME= 0;
			int uSER_SMO= 0;
			int uSER_D_12= 0;
			int d_12_PLUS1= 0;
			int uSER_DOC_DATE= 0;
			int uSER_DOCID= 0;
			int uSER_NUMDOC= 0;
			int uSER_SERDOC= 0;
			int pFR_NOTID= 0;
			int pFR_ID= 0;
			int pFR_SNILS= 0;
			int lAST_DR= 0;
			int lAST_OT= 0;
			int lAST_FAM= 0;
			int pR_OT= 0;
			int pR_FAM= 0;
			int uSER_POL= 0;
			int uSER_D_13= 0;
			int uSER_OKATO_3= 0;
			int uSER_TYPE_POL= 0;
			int pR_IM= 0;
			int lAST_IM= 0;



        	task = new TaskMock();
        	// ?????? ???????? json ??????
        	ArrayList<ArrayList<String>> list=null;

        	if(vidZaprosa.toString().contains("A08P14"))
			{
        		System.out.println("Catch A08P14");

        		 eNP_PA=0;	 pERSON_SURNAME=1;	  pERSON_KINDFIRSTNAME=2;	  pERSON_KINDLASTNAME=3; pERSON_SERDOC=10;	 pERSON_NUMDOC=11;	 dATEPASSPORT=12;	 bORN=13;	 rUSSIAN=14;	 pERSON_DOCPERSONID=15;	 pERSON_SEX=16;	 pERSON_BIRTHDAY=17;d2=19;pid29=20;
        		// for IN.1 - 1 (?????????? ??????)
        		 sMO=22;	 d_12=23;	 d_13=24;	 oKATO_3=25;	 tYPE_POL=26;	 pOL=27;
        		 // for IN.1 - 2 (????? ??????)
        		 pERSON_LINKSMOESTABLISHMENTID = 4; zAD = 5; zADPLUS40 = 6; vS_NUM = 9;

 				Message messageA08P14 = new MessageA08p14(pERSON_SERDOC, pERSON_NUMDOC, pERSON_DOCPERSONID, pERSON_SURNAME, pERSON_KINDFIRSTNAME, pERSON_KINDLASTNAME, pERSON_BIRTHDAY, pERSON_SEX, pERSON_LINKSMOESTABLISHMENTID, eNP, pERSON_ADDRESSID, pERSON_DATEINPUT, sNILS, bORN, dATEPASSPORT, eNP_PA, vS_NUM, vS_DATE, zAD, d2, sMO, d_12, d_13, oKATO_3, tYPE_POL, pOL, eNP_1, eNP_2, p14cx1, p14cx5, p14cx6, p14cx7, xPN1, xPN2, xPN3, uSERNAME, zADMINUS1, zADPLUS40, nBLANC, vS_DATEPLUS1, uSER_ENP, uSER_PERSON_SURNAME, uSER_PERSON_KINDFIRSTNAME, uSER_PERSON_KINDLASTNAME, uSER_SMO, uSER_D_12, uSER_D_13, uSER_OKATO_3, uSER_TYPE_POL, uSER_POL, rUSSIAN, d_V, d_SER, d_NUM, pR_FAM, pR_IM, pR_OT, lAST_FAM, lAST_IM, lAST_OT, lAST_DR, pFR_SNILS, pFR_ID, pFR_NOTID, uSER_SERDOC, uSER_NUMDOC, uSER_DOCID, uSER_DOC_DATE, d_12_PLUS1,pid29);
 				if(!jsonString.equals("ostalnizaprosi"))
 	        	{
 	        		list = parseStringFromList1(jsonString);
 	        	}

 				messageforallquery(messageA08P14,myoutbound,list,"");
			}

        	if(vidZaprosa.toString().equals("A08P03For"))
			{
				System.out.println("������� ?08?03For");
				Message messageA08p03For = new MessageA08p03For();

				if (task.add(userMachine))
				{
					messageforallquery(messageA08p03For,myoutbound);
				}
			}

        	if(vidZaprosa.toString().equals("A03P07"))
			{
				System.out.println("Catch A03P07");

				pERSON_SURNAME = 0;pERSON_KINDFIRSTNAME = 1;pERSON_KINDLASTNAME = 2;pERSON_BIRTHDAY = 3; pERSON_ADDRESSID= 4; int PERSON_SERPOLICY = 5; int PERSON_NUMPOLICY = 6;pERSON_SEX=7;pERSON_SERDOC = 8;pERSON_NUMDOC = 9;int PERSON_REGNUMBER = 10;pERSON_LINKSMOESTABLISHMENTID = 11;int PERSON_ESTABLISHMENTAMBUL = 12;int PERSON_DATECHANGE = 13,PERSON_ESTABLISHMENTDENT = 14,PERSON_SOCIALID = 15,PERSON_STATUSID = 16;pERSON_DOCPERSONID = 17; int PERSON_INSPECTION = 18,PERSON_OPERATION = 19,PERSON_STATUSREC = 20,PERSON_OUTID = 21,PERSON_INSPECTORID = 22,PERSON_ESTABLISHMENTID = 23,PERSON_DATEPOLICY = 24;pERSON_DATEINPUT = 25;eNP = 26;int SMO_OLD = 27,PERSONADD_ADDRESSID = 28,PERSONADD_PRIM = 29;sNILS = 30;bORN = 31;dATEPASSPORT = 32;rUSSIAN = 33;int TELEDOM = 34,TELEWORK = 35,EMAIL = 36,TELE2 = 37,DOK_VI = 38;eNP_PA= 39;int ZA = 40;zAD= 41; int ZAP = 42;int PRED = 43;d_V= 44;d_SER= 45;d_NUM= 46;int D_DATE = 47,METHOD = 48,PETITION = 49,FPOLIC = 50;pR_FAM = 51;pR_IM= 52;pR_OT= 53;int PR_TEL = 54;int PR_ADRES = 55;vS_NUM= 56;vS_DATE= 57;int D1 = 58;d2=59;int ENP_DATE = 60;lAST_FAM =61;lAST_IM= 62;lAST_OT= 63;lAST_DR= 64;int KATEG = 65,DATE_PRIK = 66,MSA =67;
				   d_13= 777; eNP_1= 777;eNP_2= 777; p14cx1= 777;p14cx5= 777;p14cx6= 777;p14cx7= 777;uSER_PERSON_SURNAME= 777;xPN1= 777;xPN2 = 777;xPN3= 777;uSERNAME= 777;zADMINUS1= 777;zADPLUS40= 777;nBLANC= 777;vS_DATEPLUS1= 777;uSER_ENP= 777;uSER_PERSON_KINDFIRSTNAME= 777;uSER_PERSON_KINDLASTNAME= 777;uSER_SMO= 777;uSER_D_12= 777; d_12_PLUS1= 777;uSER_DOC_DATE= 777;uSER_DOCID= 777;uSER_NUMDOC= 777;uSER_SERDOC= 777;pFR_NOTID= 777;pFR_ID= 777; pFR_SNILS= 777;uSER_POL= 777;uSER_D_13= 777;uSER_OKATO_3= 777;uSER_TYPE_POL= 777;oKATO_3 = 777; tYPE_POL = 777;d_12 = 777;pOL = 777;

				Message messageA03P07 = new MessageA03p07(pERSON_SERDOC, pERSON_NUMDOC, pERSON_DOCPERSONID, pERSON_SURNAME, pERSON_KINDFIRSTNAME, pERSON_KINDLASTNAME, pERSON_BIRTHDAY, pERSON_SEX, pERSON_LINKSMOESTABLISHMENTID, eNP, pERSON_ADDRESSID, pERSON_DATEINPUT, sNILS, bORN, dATEPASSPORT, eNP_PA, vS_NUM, vS_DATE, zAD, d2, sMO, d_12, d_13, oKATO_3, tYPE_POL, pOL, eNP_1, eNP_2, p14cx1, p14cx5, p14cx6, p14cx7, xPN1, xPN2, xPN3, uSERNAME, zADMINUS1, zADPLUS40, nBLANC, vS_DATEPLUS1, uSER_ENP, uSER_PERSON_SURNAME, uSER_PERSON_KINDFIRSTNAME, uSER_PERSON_KINDLASTNAME, uSER_SMO, uSER_D_12, uSER_D_13, uSER_OKATO_3, uSER_TYPE_POL, uSER_POL, rUSSIAN, d_V, d_SER, d_NUM, pR_FAM, pR_IM, pR_OT, lAST_FAM, lAST_IM, lAST_OT, lAST_DR, pFR_SNILS, pFR_ID, pFR_NOTID, uSER_SERDOC, uSER_NUMDOC, uSER_DOCID, uSER_DOC_DATE, d_12_PLUS1);

				list = parseStringFromList1(jsonString);

				messageforallquery2(messageA03P07,myoutbound,list,"A03P07");
				/*if (task.add(userMachine))
				{
					messageforallquery(messageA03P07,myoutbound);
				}*/
			}

        	if(vidZaprosa.toString().equals("A08P08"))
			{
				System.out.println("Catch A08P08");





				eNP_PA= 0;
				pERSON_SURNAME = 1;
				pERSON_KINDFIRSTNAME = 2;
				pERSON_KINDLASTNAME = 3;
				pERSON_SERDOC = 4;
				pERSON_NUMDOC = 5;
				pERSON_DOCPERSONID = 6;
				dATEPASSPORT = 7;
				bORN = 8;
				rUSSIAN = 9;
				pERSON_SEX=10;
				pERSON_BIRTHDAY = 11;
				pERSON_LINKSMOESTABLISHMENTID = 15;
				d_12 = 16;
				d_13= 17;
				tYPE_POL = 20;
				pOL = 21;
				// instead pv.4
				pERSON_ADDRESSID= 13;
				// instead pv.44
				uSER_PERSON_SURNAME = 14;
				sNILS = 25;

				d2=12;
				eNP = 0;
					int PERSON_SERPOLICY = 777; int PERSON_NUMPOLICY = 777;int PERSON_REGNUMBER = 777;int PERSON_ESTABLISHMENTAMBUL = 777;int PERSON_DATECHANGE = 777,PERSON_ESTABLISHMENTDENT = 777,PERSON_SOCIALID = 777,PERSON_STATUSID = 777;int PERSON_INSPECTION = 777,PERSON_OPERATION = 777,PERSON_STATUSREC = 777,PERSON_OUTID = 777,PERSON_INSPECTORID = 777,PERSON_ESTABLISHMENTID = 777,PERSON_DATEPOLICY = 777;pERSON_DATEINPUT = 777;int SMO_OLD = 777,PERSONADD_ADDRESSID = 777,PERSONADD_PRIM = 777;int TELEDOM = 777,TELEWORK = 777,EMAIL = 777,TELE2 = 777,DOK_VI = 777;int ZA = 777;zAD= 777; int ZAP = 777;int PRED = 777;d_V= 777;d_SER= 777;d_NUM= 777;int D_DATE = 777,METHOD = 777,PETITION = 777,FPOLIC = 777;pR_FAM = 777;pR_IM= 777;pR_OT= 777;int PR_TEL = 777;int PR_ADRES = 777;vS_NUM= 777;vS_DATE= 777;int D1 = 777;int ENP_DATE = 777;lAST_FAM =777;lAST_IM= 777;lAST_OT= 777;lAST_DR= 777;int KATEG = 777,DATE_PRIK = 777,MSA =777;
				    eNP_1= 777;eNP_2= 777; p14cx1= 777;p14cx5= 777;p14cx6= 777;p14cx7= 777;xPN1= 777;xPN2 = 777;xPN3= 777;uSERNAME= 777;zADMINUS1= 777;zADPLUS40= 777;nBLANC= 777;vS_DATEPLUS1= 777;uSER_ENP= 777;uSER_PERSON_KINDFIRSTNAME= 777;uSER_PERSON_KINDLASTNAME= 777;uSER_SMO= 777;uSER_D_12= 777; d_12_PLUS1= 777;uSER_DOC_DATE= 777;uSER_DOCID= 777;uSER_NUMDOC= 777;uSER_SERDOC= 777;pFR_NOTID= 777;pFR_ID= 777; pFR_SNILS= 777;uSER_POL= 777;uSER_D_13= 777;uSER_OKATO_3= 777;uSER_TYPE_POL= 777;oKATO_3 = 777;

				Message messageA08P08 = new MessageA08p08(pERSON_SERDOC, pERSON_NUMDOC, pERSON_DOCPERSONID, pERSON_SURNAME, pERSON_KINDFIRSTNAME, pERSON_KINDLASTNAME, pERSON_BIRTHDAY, pERSON_SEX, pERSON_LINKSMOESTABLISHMENTID, eNP, pERSON_ADDRESSID, pERSON_DATEINPUT, sNILS, bORN, dATEPASSPORT, eNP_PA, vS_NUM, vS_DATE, zAD, d2, sMO, d_12, d_13, oKATO_3, tYPE_POL, pOL, eNP_1, eNP_2, p14cx1, p14cx5, p14cx6, p14cx7, xPN1, xPN2, xPN3, uSERNAME, zADMINUS1, zADPLUS40, nBLANC, vS_DATEPLUS1, uSER_ENP, uSER_PERSON_SURNAME, uSER_PERSON_KINDFIRSTNAME, uSER_PERSON_KINDLASTNAME, uSER_SMO, uSER_D_12, uSER_D_13, uSER_OKATO_3, uSER_TYPE_POL, uSER_POL, rUSSIAN, d_V, d_SER, d_NUM, pR_FAM, pR_IM, pR_OT, lAST_FAM, lAST_IM, lAST_OT, lAST_DR, pFR_SNILS, pFR_ID, pFR_NOTID, uSER_SERDOC, uSER_NUMDOC, uSER_DOCID, uSER_DOC_DATE, d_12_PLUS1);

				list = parseStringFromList1(jsonString);

				messageforallquery2(messageA08P08,myoutbound,list,"A08P08");

			}

        	if(vidZaprosa.toString().equals("ZP9"))
			{
				System.out.println("??????? ZP9");
				Message messageZP9 = new MessageZp9();

				if (task.add(userMachine))
				{
					messageforallquery(messageZP9,myoutbound);
				}
			}

        	if(vidZaprosa.toString().equals("A08P03kol"))
			{
				System.out.println("??????? ?08?03kol");
				Message messageA08P03 = new MessageA08p03kol();

				if (task.add(userMachine))
				{
					messageforallquery(messageA08P03,myoutbound);
				}
			}


        	if(vidZaprosa.toString().equals("A08P06"))
			{
				System.out.println("??????? A08P06");
				Message messageA08P06 = new MessageA08p06();

				if (task.add(userMachine))
				{
					messageforallquery(messageA08P06,myoutbound);
				}
			}
        	if(vidZaprosa.toString().equals("A08P02"))
			{
				System.out.println("������� A08P02");
				Message messageA08P02 = new MessageA08p02();

				//if (task.add(userMachine))
				//{
					messageforallquery(messageA08P02,myoutbound);
				//}
			}


        	if(vidZaprosa.toString().equals("buttonZP9"))
			{
        		if(!jsonString.equals("ostalnizaprosi"))
	        	{
        			if(jsonString.contains("list1enpzp9"))
        			{
        				pERSON_SURNAME = 0;pERSON_KINDFIRSTNAME = 1;pERSON_KINDLASTNAME = 2;pERSON_BIRTHDAY = 3; pERSON_ADDRESSID= 4; int PERSON_SERPOLICY = 5; int PERSON_NUMPOLICY = 6;pERSON_SEX=7;pERSON_SERDOC = 8;pERSON_NUMDOC = 9;int PERSON_REGNUMBER = 10;pERSON_LINKSMOESTABLISHMENTID = 11;int PERSON_ESTABLISHMENTAMBUL = 12;int PERSON_DATECHANGE = 13,PERSON_ESTABLISHMENTDENT = 14,PERSON_SOCIALID = 15,PERSON_STATUSID = 16;pERSON_DOCPERSONID = 17; int PERSON_INSPECTION = 18,PERSON_OPERATION = 19,PERSON_STATUSREC = 20,PERSON_OUTID = 21,PERSON_INSPECTORID = 22,PERSON_ESTABLISHMENTID = 23,PERSON_DATEPOLICY = 24;pERSON_DATEINPUT = 25;eNP = 26;int SMO_OLD = 27,PERSONADD_ADDRESSID = 28,PERSONADD_PRIM = 29;sNILS = 30;bORN = 31;dATEPASSPORT = 32;rUSSIAN = 33;int TELEDOM = 34,TELEWORK = 35,EMAIL = 36,TELE2 = 37,DOK_VI = 38;eNP_PA= 39;int ZA = 40;zAD= 41; int ZAP = 42;int PRED = 43;d_V= 44;d_SER= 45;d_NUM= 46;int D_DATE = 47,METHOD = 48,PETITION = 49,FPOLIC = 50;pR_FAM = 51;pR_IM= 52;pR_OT= 53;int PR_TEL = 54;int PR_ADRES = 55;vS_NUM= 56;vS_DATE= 57;int D1 = 58;d2=59;int ENP_DATE = 60;lAST_FAM =61;lAST_IM= 62;lAST_OT= 63;lAST_DR= 64;int KATEG = 65,DATE_PRIK = 66,MSA =67;
       				   d_13= 777; eNP_1= 777;eNP_2= 777; p14cx1= 777;p14cx5= 777;p14cx6= 777;p14cx7= 777;uSER_PERSON_SURNAME= 777;xPN1= 777;xPN2 = 777;xPN3= 777;uSERNAME= 777;zADMINUS1= 777;zADPLUS40= 777;nBLANC= 777;vS_DATEPLUS1= 777;uSER_ENP= 777;uSER_PERSON_KINDFIRSTNAME= 777;uSER_PERSON_KINDLASTNAME= 777;uSER_SMO= 777;uSER_D_12= 777; d_12_PLUS1= 777;uSER_DOC_DATE= 777;uSER_DOCID= 777;uSER_NUMDOC= 777;uSER_SERDOC= 777;pFR_NOTID= 777;pFR_ID= 777; pFR_SNILS= 777;uSER_POL= 777;uSER_D_13= 777;uSER_OKATO_3= 777;uSER_TYPE_POL= 777;oKATO_3 = 777; tYPE_POL = 777;d_12 = 777;pOL = 777;

        				jsonString = jsonString.replaceAll("list1enpzp9", "list1");
        				System.out.println("??????? buttonZP9");
            			System.out.println("DataEnp "+ jsonString);

            			list = parseStringFromList1(jsonString);
            			System.out.println("parse into ArrayList<ArrayList<String>> "+list);

            			Message messageZP9enp = new MessageZp9(pERSON_SERDOC, pERSON_NUMDOC, pERSON_DOCPERSONID, pERSON_SURNAME, pERSON_KINDFIRSTNAME, pERSON_KINDLASTNAME, pERSON_BIRTHDAY, pERSON_SEX, pERSON_LINKSMOESTABLISHMENTID, eNP, pERSON_ADDRESSID, pERSON_DATEINPUT, sNILS, bORN, dATEPASSPORT, eNP_PA, vS_NUM, vS_DATE, zAD, d2, sMO, d_12, d_13, oKATO_3, tYPE_POL, pOL, eNP_1, eNP_2, p14cx1, p14cx5, p14cx6, p14cx7, xPN1, xPN2, xPN3, uSERNAME, zADMINUS1, zADPLUS40, nBLANC, vS_DATEPLUS1, uSER_ENP, uSER_PERSON_SURNAME, uSER_PERSON_KINDFIRSTNAME, uSER_PERSON_KINDLASTNAME, uSER_SMO, uSER_D_12, uSER_D_13, uSER_OKATO_3, uSER_TYPE_POL, uSER_POL, rUSSIAN, d_V, d_SER, d_NUM, pR_FAM, pR_IM, pR_OT, lAST_FAM, lAST_IM, lAST_OT, lAST_DR, pFR_SNILS, pFR_ID, pFR_NOTID, uSER_SERDOC, uSER_NUMDOC, uSER_DOCID, uSER_DOC_DATE, d_12_PLUS1);
            			messageforallquery2(messageZP9enp,myoutbound,list,"list1enpzp9");
        			}

        			if(jsonString.contains("list1passportzp9"))
        			{
        				pERSON_SURNAME = 0;pERSON_KINDFIRSTNAME = 1;pERSON_KINDLASTNAME = 2;pERSON_BIRTHDAY = 3; pERSON_ADDRESSID= 4; int PERSON_SERPOLICY = 5; int PERSON_NUMPOLICY = 6;pERSON_SEX=7;pERSON_SERDOC = 8;pERSON_NUMDOC = 9;int PERSON_REGNUMBER = 10;pERSON_LINKSMOESTABLISHMENTID = 11;int PERSON_ESTABLISHMENTAMBUL = 12;int PERSON_DATECHANGE = 13,PERSON_ESTABLISHMENTDENT = 14,PERSON_SOCIALID = 15,PERSON_STATUSID = 16;pERSON_DOCPERSONID = 17; int PERSON_INSPECTION = 18,PERSON_OPERATION = 19,PERSON_STATUSREC = 20,PERSON_OUTID = 21,PERSON_INSPECTORID = 22,PERSON_ESTABLISHMENTID = 23,PERSON_DATEPOLICY = 24;pERSON_DATEINPUT = 25;eNP = 26;int SMO_OLD = 27,PERSONADD_ADDRESSID = 28,PERSONADD_PRIM = 29;sNILS = 30;bORN = 31;dATEPASSPORT = 32;rUSSIAN = 33;int TELEDOM = 34,TELEWORK = 35,EMAIL = 36,TELE2 = 37,DOK_VI = 38;eNP_PA= 39;int ZA = 40;zAD= 41; int ZAP = 42;int PRED = 43;d_V= 44;d_SER= 45;d_NUM= 46;int D_DATE = 47,METHOD = 48,PETITION = 49,FPOLIC = 50;pR_FAM = 51;pR_IM= 52;pR_OT= 53;int PR_TEL = 54;int PR_ADRES = 55;vS_NUM= 56;vS_DATE= 57;int D1 = 58;d2=59;int ENP_DATE = 60;lAST_FAM =61;lAST_IM= 62;lAST_OT= 63;lAST_DR= 64;int KATEG = 65,DATE_PRIK = 66,MSA =67;
      				   d_13= 777; eNP_1= 777;eNP_2= 777; p14cx1= 777;p14cx5= 777;p14cx6= 777;p14cx7= 777;uSER_PERSON_SURNAME= 777;xPN1= 777;xPN2 = 777;xPN3= 777;uSERNAME= 777;zADMINUS1= 777;zADPLUS40= 777;nBLANC= 777;vS_DATEPLUS1= 777;uSER_ENP= 777;uSER_PERSON_KINDFIRSTNAME= 777;uSER_PERSON_KINDLASTNAME= 777;uSER_SMO= 777;uSER_D_12= 777; d_12_PLUS1= 777;uSER_DOC_DATE= 777;uSER_DOCID= 777;uSER_NUMDOC= 777;uSER_SERDOC= 777;pFR_NOTID= 777;pFR_ID= 777; pFR_SNILS= 777;uSER_POL= 777;uSER_D_13= 777;uSER_OKATO_3= 777;uSER_TYPE_POL= 777;oKATO_3 = 777; tYPE_POL = 777;d_12 = 777;pOL = 777;

      				   jsonString = jsonString.replaceAll("list1passportzp9", "list1");
        				System.out.println("??????? buttonZP9");
            			System.out.println("DataPassport "+ jsonString);

            			list = parseStringFromList1(jsonString);
            			System.out.println("parse into ArrayList<ArrayList<String>> "+list);

            			Message messageZP9passport = new MessageZp9(pERSON_SERDOC, pERSON_NUMDOC, pERSON_DOCPERSONID, pERSON_SURNAME, pERSON_KINDFIRSTNAME, pERSON_KINDLASTNAME, pERSON_BIRTHDAY, pERSON_SEX, pERSON_LINKSMOESTABLISHMENTID, eNP, pERSON_ADDRESSID, pERSON_DATEINPUT, sNILS, bORN, dATEPASSPORT, eNP_PA, vS_NUM, vS_DATE, zAD, d2, pERSON_LINKSMOESTABLISHMENTID, d_12, d_13, oKATO_3, tYPE_POL, pOL, eNP_1, eNP_2, p14cx1, p14cx5, p14cx6, p14cx7, xPN1, xPN2, xPN3, uSERNAME, zADMINUS1, zADPLUS40, nBLANC, vS_DATEPLUS1, uSER_ENP, uSER_PERSON_SURNAME, uSER_PERSON_KINDFIRSTNAME, uSER_PERSON_KINDLASTNAME, uSER_SMO, uSER_D_12, uSER_D_13, uSER_OKATO_3, uSER_TYPE_POL, uSER_POL, rUSSIAN, d_V, d_SER, d_NUM, pR_FAM, pR_IM, pR_OT, lAST_FAM, lAST_IM, lAST_OT, lAST_DR, pFR_SNILS, pFR_ID, pFR_NOTID, uSER_SERDOC, uSER_NUMDOC, uSER_DOCID, uSER_DOC_DATE, d_12_PLUS1);
            			messageforallquery2(messageZP9passport,myoutbound,list,"list1passportzp9");
        			}

        			if(jsonString.contains("list1snilszp9"))
        			{
        				pERSON_SURNAME = 0;pERSON_KINDFIRSTNAME = 1;pERSON_KINDLASTNAME = 2;pERSON_BIRTHDAY = 3; pERSON_ADDRESSID= 4; int PERSON_SERPOLICY = 5; int PERSON_NUMPOLICY = 6;pERSON_SEX=7;pERSON_SERDOC = 8;pERSON_NUMDOC = 9;int PERSON_REGNUMBER = 10;pERSON_LINKSMOESTABLISHMENTID = 11;int PERSON_ESTABLISHMENTAMBUL = 12;int PERSON_DATECHANGE = 13,PERSON_ESTABLISHMENTDENT = 14,PERSON_SOCIALID = 15,PERSON_STATUSID = 16;pERSON_DOCPERSONID = 17; int PERSON_INSPECTION = 18,PERSON_OPERATION = 19,PERSON_STATUSREC = 20,PERSON_OUTID = 21,PERSON_INSPECTORID = 22,PERSON_ESTABLISHMENTID = 23,PERSON_DATEPOLICY = 24;pERSON_DATEINPUT = 25;eNP = 26;int SMO_OLD = 27,PERSONADD_ADDRESSID = 28,PERSONADD_PRIM = 29;sNILS = 30;bORN = 31;dATEPASSPORT = 32;rUSSIAN = 33;int TELEDOM = 34,TELEWORK = 35,EMAIL = 36,TELE2 = 37,DOK_VI = 38;eNP_PA= 39;int ZA = 40;zAD= 41; int ZAP = 42;int PRED = 43;d_V= 44;d_SER= 45;d_NUM= 46;int D_DATE = 47,METHOD = 48,PETITION = 49,FPOLIC = 50;pR_FAM = 51;pR_IM= 52;pR_OT= 53;int PR_TEL = 54;int PR_ADRES = 55;vS_NUM= 56;vS_DATE= 57;int D1 = 58;d2=59;int ENP_DATE = 60;lAST_FAM =61;lAST_IM= 62;lAST_OT= 63;lAST_DR= 64;int KATEG = 65,DATE_PRIK = 66,MSA =67;
       				    d_13= 777; eNP_1= 777;eNP_2= 777; p14cx1= 777;p14cx5= 777;p14cx6= 777;p14cx7= 777;uSER_PERSON_SURNAME= 777;xPN1= 777;xPN2 = 777;xPN3= 777;uSERNAME= 777;zADMINUS1= 777;zADPLUS40= 777;nBLANC= 777;vS_DATEPLUS1= 777;uSER_ENP= 777;uSER_PERSON_KINDFIRSTNAME= 777;uSER_PERSON_KINDLASTNAME= 777;uSER_SMO= 777;uSER_D_12= 777; d_12_PLUS1= 777;uSER_DOC_DATE= 777;uSER_DOCID= 777;uSER_NUMDOC= 777;uSER_SERDOC= 777;pFR_NOTID= 777;pFR_ID= 777; pFR_SNILS= 777;uSER_POL= 777;uSER_D_13= 777;uSER_OKATO_3= 777;uSER_TYPE_POL= 777;oKATO_3 = 777; tYPE_POL = 777;d_12 = 777;pOL = 777;

        				jsonString = jsonString.replaceAll("list1snilszp9", "list1");
        				System.out.println("??????? buttonZP9");
            			System.out.println("DataPassport "+ jsonString);

            			list = parseStringFromList1(jsonString);
            			System.out.println("parse into ArrayList<ArrayList<String>> "+list);

            			Message messageZP9snils = new MessageZp9(pERSON_SERDOC, pERSON_NUMDOC, pERSON_DOCPERSONID, pERSON_SURNAME, pERSON_KINDFIRSTNAME, pERSON_KINDLASTNAME, pERSON_BIRTHDAY, pERSON_SEX, pERSON_LINKSMOESTABLISHMENTID, eNP, pERSON_ADDRESSID, pERSON_DATEINPUT, sNILS, bORN, dATEPASSPORT, eNP_PA, vS_NUM, vS_DATE, zAD, d2, pERSON_LINKSMOESTABLISHMENTID, d_12, d_13, oKATO_3, tYPE_POL, pOL, eNP_1, eNP_2, p14cx1, p14cx5, p14cx6, p14cx7, xPN1, xPN2, xPN3, uSERNAME, zADMINUS1, zADPLUS40, nBLANC, vS_DATEPLUS1, uSER_ENP, uSER_PERSON_SURNAME, uSER_PERSON_KINDFIRSTNAME, uSER_PERSON_KINDLASTNAME, uSER_SMO, uSER_D_12, uSER_D_13, uSER_OKATO_3, uSER_TYPE_POL, uSER_POL, rUSSIAN, d_V, d_SER, d_NUM, pR_FAM, pR_IM, pR_OT, lAST_FAM, lAST_IM, lAST_OT, lAST_DR, pFR_SNILS, pFR_ID, pFR_NOTID, uSER_SERDOC, uSER_NUMDOC, uSER_DOCID, uSER_DOC_DATE, d_12_PLUS1);
            			messageforallquery2(messageZP9snils,myoutbound,list,"list1snilszp9");
        			}
	        	}
			}

        	if(vidZaprosa.toString().equals("A08P02test"))
			{
        		 eNP_PA=0;	 pERSON_SURNAME=1;	  pERSON_KINDFIRSTNAME=2;	  pERSON_KINDLASTNAME=3;	 sMO=4;	 d_12=5;	 d_13=6;	 oKATO_3=7;	 tYPE_POL=8;	 pOL=9;	 pERSON_SERDOC=10;	 pERSON_NUMDOC=11;	 dATEPASSPORT=12;	 bORN=13;	 rUSSIAN=14;	 pERSON_DOCPERSONID=15;	 pERSON_SEX=16;	 pERSON_BIRTHDAY=17;d2=19;pid29=20;


				System.out.println("������� A08P02test");
				Message messageA08P02 = new MessageA08p02(pERSON_SERDOC, pERSON_NUMDOC, pERSON_DOCPERSONID, pERSON_SURNAME, pERSON_KINDFIRSTNAME, pERSON_KINDLASTNAME, pERSON_BIRTHDAY, pERSON_SEX, pERSON_LINKSMOESTABLISHMENTID, eNP, pERSON_ADDRESSID, pERSON_DATEINPUT, sNILS, bORN, dATEPASSPORT, eNP_PA, vS_NUM, vS_DATE, zAD, d2, sMO, d_12, d_13, oKATO_3, tYPE_POL, pOL, eNP_1, eNP_2, p14cx1, p14cx5, p14cx6, p14cx7, xPN1, xPN2, xPN3, uSERNAME, zADMINUS1, zADPLUS40, nBLANC, vS_DATEPLUS1, uSER_ENP, uSER_PERSON_SURNAME, uSER_PERSON_KINDFIRSTNAME, uSER_PERSON_KINDLASTNAME, uSER_SMO, uSER_D_12, uSER_D_13, uSER_OKATO_3, uSER_TYPE_POL, uSER_POL, rUSSIAN, d_V, d_SER, d_NUM, pR_FAM, pR_IM, pR_OT, lAST_FAM, lAST_IM, lAST_OT, lAST_DR, pFR_SNILS, pFR_ID, pFR_NOTID, uSER_SERDOC, uSER_NUMDOC, uSER_DOCID, uSER_DOC_DATE, d_12_PLUS1,pid29);
				if(!jsonString.equals("ostalnizaprosi"))
	        	{
	        		list = parseStringFromList1(jsonString);
	        	}

				messageforallquery(messageA08P02,myoutbound,list,"");
			}

        	if(vidZaprosa.toString().equals("A08B01"))
			{
				System.out.println("catch A08B01");
				Message messageA08B01 = new MessageA08v01();

				if (task.add(userMachine))
				{
					messageforallquery(messageA08B01,myoutbound);
				}
			}

        	if(vidZaprosa.toString().equals("ZP1Fiod"))
			{
				System.out.println("catch ZP1Fiod");
				Message messageZP1Fiod = new MessageZp1Fiod();

				if (task.add(userMachine))
				{
					messageforallqueryZP1Fiod(messageZP1Fiod,myoutbound);


				}
			}

        	if(vidZaprosa.toString().equals("A24P102"))
			{
				System.out.println("catch A24P102");
				Message messageA24P102 = new MessageA24p102();

				if (task.add(userMachine))
				{
					messageforallquery(messageA24P102,myoutbound);
				}
			}

        	if(vidZaprosa.toString().equals("ZP1pr"))
			{
				System.out.println("catch ZP1pr");
				Message messageZP1pr = new MessageZp1pr();

				if (task.add(userMachine))
				{
					messageforallquery(messageZP1pr,myoutbound);
				}
			}

        	if(vidZaprosa.toString().equals("A24P10"))
			{
				System.out.println("catch A24P10");
				Message messageA24P10 = new MessageA24p10();

				if (task.add(userMachine))
				{
					messageforallquery(messageA24P10,myoutbound);
				}
			}

			if(vidZaprosa.toString().equals("A08P01"))
			{
				System.out.println("catch A08P01");
				Message messageA08P01 = new MessageA08p01();

				if (task.add(userMachine))
				{
					messageforallquery(messageA08P01,myoutbound);
				}
			}

			if(vidZaprosa.toString().equals("P27"))
			{
				System.out.println("catch P27");
				Message messageP27 = new MessageP27();

				if (task.add(userMachine))
				{
					messageforallquery(messageP27,myoutbound);
				}
			}

			if(vidZaprosa.toString().equals("P26"))
			{
				System.out.println("catch P26");
				Message messageP26 = new MessageP26();

				if (task.add(userMachine))
				{
					messageforallquery(messageP26,myoutbound);
				}
			}

			if(vidZaprosa.toString().equals("A24P10com"))
			{
				System.out.println("catch A24P10com");
				Message messageA24P10 = new MessageA24p10();
				Message messageA24p102 = new MessageA24p102();

				if (task.add(userMachine))
				{
					messageforallquery(messageA24P10,myoutbound);
					messageforallquery(messageA24p102,myoutbound);
				}
			}

			if(vidZaprosa.toString().equals("A08P03pr"))
			{
				System.out.println("catch A08P03pr");
				Message messageA08P03pr = new MessageA08p03pr();

				if (task.add(userMachine))
				{
					messageforallquery(messageA08P03pr,myoutbound);
				}
			}

			if(vidZaprosa.toString().equals("A13P09"))
			{
				System.out.println("catch A13P09");
				Message messageA13P09 = new MessageA13p09();

				if (task.add(userMachine))
				{
					messageforallquery(messageA13P09,myoutbound);
				}
			}

			if(vidZaprosa.toString().equals("A08P04"))
			{
				System.out.println("catch A08P04");
				Message messageA08P04 = new MessageA08p04();

				if (task.add(userMachine))
				{
					messageforallquery(messageA08P04,myoutbound);
				}
			}

			if(vidZaprosa.toString().equals("A08P16"))
			{
				System.out.println("catch A08P16");
				Message messageA08P16 = new MessageA08p16();

				//���� ������� �������� ������ � ������� xml_task
				if (task.add(userMachine))
				{
					messageforallquery(messageA08P16,myoutbound);
				}
			}

			if(vidZaprosa.toString().equals("A08P03"))
			{
				System.out.println("catch A08P03");
				Message messageA08P03 = new MessageA08p03();

				if (task.add(userMachine))
				{
					messageforallquery(messageA08P03,myoutbound);
				}
			}

			if(vidZaprosa.toString().equals("ZP3"))
			{
				System.out.println("catch ZP3");
				Message messageZp3 = new MessageZp3();


				ArrayList<ArrayList<String>> ls = new ArrayList<ArrayList<String>>();
				messageNoDate(messageZp3,myoutbound,ls,"ZP3");

			}


        }

		/**
		 * ����� "����" ������ uorak2, ��� ��������� (������) ����������� ��� �������� ����������.
		 * �������� ����� ������� ��� ZP3, ZP2 � ��� �����
		 * @throws IOException
		 */
		private void messageNoDate(Message mes, WsOutbound myoutbound,	ArrayList<ArrayList<String>> list, String kluch) throws Exception {

			if(mes.create(userMachine, list,kluch)){
				// 50000-A81268FD-7AFA-4624-BDB0-C9796596D343
				/*String file = "50000-A6F025D6-9AD3-1964-FF37-460721765516" + ".uprmes";
				String fileUpr2 = "50000-A6F025D6-9AD3-1964-FF37-460721765516";
				String sentMessages = "";
				File to_file = new File(Const.OUTPUTDONE + fileUpr2 + ".uprak2");*/


				String file = "50000-" + mes.getGuidBhs() + ".uprmes";
				String fileUpr2 = "50000-" + mes.getGuidBhs();
				String sentMessages = "";
				File to_file = new File(Const.OUTPUTDONE + fileUpr2 + ".uprak2");

				CharBuffer buffer3 = CharBuffer.wrap("��������� " + file);
				myoutbound.writeTextMessage(buffer3);

				if (ConstantiNastrojki.otladkaXML.equals("0")) {

					while ("".equals(sentMessages)) {

						try {
							Thread.sleep(20000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						if (to_file.exists()) {
							sentMessages = "> ������ " + to_file.toString();
							CharBuffer buffer5 = CharBuffer.wrap(sentMessages);
							myoutbound.writeTextMessage(buffer5);

							sentMessages = "> ��������� � ��������� ���������� ���";
							buffer5 = CharBuffer.wrap(sentMessages);
							myoutbound.writeTextMessage(buffer5);

							services.zp3process(fileUpr2,"uprak2");


							sentMessages = "> �������� �� ������ ���� " + fileUpr2 + ".zp3";
							buffer5 = CharBuffer.wrap(sentMessages);
							myoutbound.writeTextMessage(buffer5);


						} else {
							CharBuffer buffer6 = CharBuffer
									.wrap("> ������� ������ uprak2");
							myoutbound.writeTextMessage(buffer6);
						}
					}
				}else {
					CharBuffer buffer5 = CharBuffer.wrap("> ����� �������");
					myoutbound.writeTextMessage(buffer5);
				}
			}
		}

		private void messageforallquery(Message mes, WsOutbound myoutbound,	ArrayList<ArrayList<String>> list, String kluch) throws Exception {
			System.out.println("MEssage for all query");
			if (mes.create(userMachine, list,kluch))
			{
				String file = "50000-" + mes.getGuidBhs() + ".uprmes";
				String fileUpr2 = "50000-" + mes.getGuidBhs();
				String sentMessages = "";
				File to_file = new File(Const.OUTPUTDONE + fileUpr2 + ".uprak2");

				CharBuffer buffer3 = CharBuffer.wrap("!��������� " + file);
				myoutbound.writeTextMessage(buffer3);

				if (ConstantiNastrojki.otladkaXML.equals("0")) {
					while ("".equals(sentMessages)) {
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						if (to_file.exists()) {
							sentMessages = "> ������ " + to_file.toString();
							CharBuffer buffer5 = CharBuffer.wrap(sentMessages);
							myoutbound.writeTextMessage(buffer5);

							CharBuffer buffer66 = CharBuffer.wrap("q");
							myoutbound.writeTextMessage(buffer66);
						} else {
							CharBuffer buffer6 = CharBuffer
									.wrap("������� ������ uprak2");
							myoutbound.writeTextMessage(buffer6);
						}
					}
				} else {
					CharBuffer buffer5 = CharBuffer.wrap("> ����� �������");
					myoutbound.writeTextMessage(buffer5);
				}
			}
		}

		private void messageforallquery2(Message mes, WsOutbound myoutbound,
				ArrayList<ArrayList<String>> list, String kluch) throws Exception {
			System.out.println("Message for all query2");
			if (mes.create(userMachine, list,kluch)) {

				UtilParseDbXml utilparsedbXml = new UtilParseDbXml();

				String file = "50000-" + mes.getGuidBhs() + ".uprmes";
				String fileUpr2 = "50000-" + mes.getGuidBhs();
				String sentMessages = "";
				File to_file = new File(Const.OUTPUTDONE + fileUpr2 + ".uprak2");

				utilparsedbXml.marshaling("50000-" + mes.getGuidBhs(),mes.getDataList());

				CharBuffer buffer3 = CharBuffer.wrap("��������� " + file);
				myoutbound.writeTextMessage(buffer3);

				if (ConstantiNastrojki.otladkaXML.equals("0"))
				{
					while ("".equals(sentMessages)) {
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						if (to_file.exists()) {
							sentMessages = "> ������� " + fileUpr2+".uprak2 "+ kluch;
							CharBuffer buffer5 = CharBuffer.wrap(sentMessages);
							//

							/*
							 * ��������  � wbAnswer.js json ������ Uprmes'a (��������� ������� ��������� uprmess)
							 * �� ������ ���� ������ ����������� ������ ���� web ������
							 *
							 */
							// ���������� � ����� ���� ����� ������� �� ������� � ���������� ������
							CharBuffer buffer777 = CharBuffer.wrap("50000-" + mes.getGuidBhs()+".fromdbforuprmess");
							// ������� �� ����� wbAnswer.js  c ������� ��� ������ (������ ������� �� ����)
							myoutbound.writeTextMessage(buffer777);
							// ���������� ��������� ��� ������� �����2
							myoutbound.writeTextMessage(buffer5);
						}
						else
						{
							CharBuffer budfer232 = CharBuffer
									.wrap("> ������� ������ uprak2");
							myoutbound.writeTextMessage(budfer232);
						}
					}
				} else {
					CharBuffer buffer5 = CharBuffer.wrap("> ����� �������");
					myoutbound.writeTextMessage(buffer5);
				}
			}
		}


		/*
		 * ����������� ��� ������� ZP1Fiod
		 *
		 */
		private void messageforallqueryZP1Fiod(Message mes, WsOutbound myoutbound) throws Exception {
			if (mes.create(userMachine)) {
				 new AnswerData().loadToExcel(mes.getDataList(), userMachine +".xls");
				String file = "50000-" + mes.getGuidBhs() + ".uprmes";
				String fileUpr2 = "50000-" + mes.getGuidBhs();
				String sentMessages = "";
				File to_file = new File(Const.OUTPUTDONE + fileUpr2 + ".uprak2");

				CharBuffer buffer3 = CharBuffer.wrap("��������� " + file);
				myoutbound.writeTextMessage(buffer3);

				if (ConstantiNastrojki.otladkaXML.equals("0")) {
					while ("".equals(sentMessages)) {
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						if (to_file.exists()) {
							sentMessages = "> ������� " + to_file.toString();
							CharBuffer buffer5 = CharBuffer.wrap(sentMessages);
							myoutbound.writeTextMessage(buffer5);

							//CharBuffer buffer66 = CharBuffer.wrap("q");
							//myoutbound.writeTextMessage(buffer66);
						} else {
							CharBuffer buffer6 = CharBuffer
									.wrap("> ������� ������ uprak2");
							myoutbound.writeTextMessage(buffer6);
						}
					}

						Load zpLoad = new ZpLoadMock();
						if (zpLoad.load(fileUpr2)) {
							String excelFile = userMachine + ".xls";
							File excelFileEssence = new File("\\\\asu-srp\\ErzNsk_all_the_rest\\excel\\" + excelFile);
							if (excelFileEssence.exists()) {
								new AnswerZp().loadToExcel(userMachine);
								CharBuffer buffer4 = CharBuffer
										.wrap("> ��������� � ������");
								myoutbound.writeTextMessage(buffer4);
								System.out.println("���� �k���� "
										+ excelFileEssence);
								// ???? ???? ?????? 1 ??? 0
								if (listKluchi.get(1).equals("1")) {
									if (Desktop.isDesktopSupported()) {
										Desktop.getDesktop().open(
												excelFileEssence);
									}

								}

							} else {
								System.out
										.println("�������� � ������ �� ���������");
							}

						} else {
							System.out.println("������ � �������� ���2");
						}

				} else {
					CharBuffer buffer5 = CharBuffer.wrap("> ����� �������");
					myoutbound.writeTextMessage(buffer5);
				}
			}
		}


		private void messageforallquery(Message mes, WsOutbound myoutbound) throws Exception {
			if (mes.create(userMachine)) {
				 new AnswerData().loadToExcel(mes.getDataList(), userMachine +".xls");
				String file = "50000-" + mes.getGuidBhs() + ".uprmes";
				String fileUpr2 = "50000-" + mes.getGuidBhs();
				String sentMessages = "";
				File to_file = new File(Const.OUTPUTDONE + fileUpr2 + ".uprak2");

				CharBuffer buffer3 = CharBuffer.wrap("��������� " + file);
				myoutbound.writeTextMessage(buffer3);

				if (ConstantiNastrojki.otladkaXML.equals("0")) {
					while ("".equals(sentMessages)) {
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						if (to_file.exists()) {
							sentMessages = "> ������� " + to_file.toString();
							CharBuffer buffer5 = CharBuffer.wrap(sentMessages);
							myoutbound.writeTextMessage(buffer5);

							CharBuffer buffer66 = CharBuffer.wrap("q");
							myoutbound.writeTextMessage(buffer66);
						} else {
							CharBuffer buffer6 = CharBuffer
									.wrap("> ������� ������ uprak2");
							myoutbound.writeTextMessage(buffer6);
						}
					}
				} else {
					CharBuffer buffer5 = CharBuffer.wrap("> ����� �������");
					myoutbound.writeTextMessage(buffer5);
				}
			}
		}

		@SuppressWarnings("unchecked")
		private ArrayList<ArrayList<String>> parseStringFromList1(String jsonString) throws JsonParseException,
				JsonMappingException, IOException, ParseException {
			//System.out.println("jsonString:" + jsonString);
			jsonString = jsonString.replaceAll("\\?", "?").replaceAll("[^A-Za-z?-??-?0-9- -.-:-\\]-\\[-{-}-�-]", "");
			//System.out.println("jsonStringAfter:" + jsonString);
			// initiate jackson mapper
			ObjectMapper mapper = new ObjectMapper();
			// Convert received JSON to Article
			ListWebForXMLQuery article = mapper.readValue(jsonString,ListWebForXMLQuery.class);
			System.out.println(article);
			// ���������� � ��������� �.�. ��������� �� ������� ���� ������
			// ������ �� �������
			ArrayList<List<Object>> web = new ArrayList<List<Object>>();
			web.add(article.getList1());

			ArrayList<ArrayList<String>> dataList = new ArrayList<ArrayList<String>>();
			DateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy");
			// ����� �� ������� � ��������� � dataList
			for (int i = 0; i < web.get(0).size(); i++)
			{
				// ����������� � ���� ���� � ������ ������
				ArrayList<String> colums = (ArrayList<String>) web.get(0).get(i);
				for(int j = 0; j < colums.size(); j++)
				{
					// ?????? ?? NULL POINTER EXEPTION
					if(colums.get(j)== null){colums.set(j, "");}
					// �������� �������� ��������� /
					if(colums.get(j).equalsIgnoreCase("��")){colums.set(j, "�/�");}
					//System.out.println(colums.get(j));
					if(colums.get(j).trim().length() == 10 && colums.get(j).substring(2, 3).equals(".") && colums.get(j).substring(5, 6).equals("."))
					{
						colums.set(j, new SimpleDateFormat("yyyy-MM-dd").format(inputFormat.parse(colums.get(j))	));
						web.get(0).set(i, colums);

					}
				}

				dataList.add((ArrayList<String>) web.get(0).get(i));
			}
			System.out.println("parseStringFromList1 OK!");
			return dataList;
		}


	}

}



