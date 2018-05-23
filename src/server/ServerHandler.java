package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerHandler implements Runnable {
	public static final int SERVERPORT = 51701;
	public static Socket client = null;

	public void run() {
		char dataType;
		String str = null;
		try {
			System.out.println("S: Receiving...");
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(SERVERPORT);

			while (true) {
				client = serverSocket.accept();
				System.out.println("S: Receiving...");
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));

					// 读取客户端的信息
					str = in.readLine();
					dataType = str.charAt(0);
					System.out.println("the str is:" + str);

					switch (dataType) {
					case '0':
						String res = null;
						int check = 0;
						PrintWriter out = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8")), true);

						// 先查询用户是否存在
						res = mysqlConnect.isUsersNameAndPwdExist(str);
						// 用户存在
						if (res != null) {
							check = mysqlConnect.updateUserState(str, 1);
							// 用户登录状态修改成功,返回当前用户信息，包括user_name和level
							if (check == 1) {
								System.out.println("用户状态更新成功！");
								out.println(res);
								out.flush();
							}
						} else {
							// 用户不存在返回null
							out.println("null");
							out.flush();
						}
						break;

					case '1':
						int res1 = 0, check1 = 0;
						PrintWriter out1 = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8")), true);

						// 先查询用户名是否已经被注册
						check1 = mysqlConnect.isUsersNameExist(str);
						// 用户名已经被注册。返回-1
						if (check1 == 1) {
							System.out.println("该用户名已经被注册");
							out1.println(300);
							out1.flush();
						}
						// 用户名未被注册且插入成功，返回1
						else {
							System.out.println("该用户名未被注册");
							res1 = mysqlConnect.insertToUsers(str);
							System.out.println(res1);
							out1.println(201);
							out1.flush();
						}
						break;

					case '2':
						String sent = "";
						PrintWriter out2 = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8")), true);

						System.out.println("查找字符串：" + str);
						sent = mysqlConnect.searchWord(str);
						if (sent.equals("")) {
							System.out.println("There is nothing!");
							out2.println("null");
							out2.flush();
						} else {
							out2.println(sent);
							out2.flush();
						}

						break;

					case '3':
						int check3 = 0;
						PrintWriter out3 = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8")), true);
						
						String[] strArray = str.split(";");
						for (int i = 0; i < strArray.length; i++) {
							System.out.println(strArray[i]);
							// 先查询当前数据库里是否已经存在该解释
							check3 = mysqlConnect.isParaExist(strArray[i]);
							if (check3 == 1) {
								System.out.println("已经存在该解释");
								out3.println(301);
								out3.flush();
							} else {
								mysqlConnect.insertToDict(strArray[i]);
								System.out.println("解释插入成功");
								out3.println(202);
								out3.flush();
							}
						}
						break;

					case '4':
						int check4 = 0;
						PrintWriter out4 = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8")), true);
						System.out.println("the str is:" + str);
						check4 = mysqlConnect.checkLikeOperation(str);
						System.out.println("check like:" + check4);
						
						if(check4 == 0) {
							System.out.println("用户还未点过赞");
							out4.println(204);
							out4.flush();
						}else {
							System.out.println("用户已经点过赞");
							out4.println(304);
							out4.flush();
						}
						break;
						
					case '5':
						int check5 = 0;
						PrintWriter out5 = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8")), true);
						check5 = mysqlConnect.checkHateOperation(str);
						System.out.println("check hate:" + check5);
						
						if(check5 == 0) {
							System.out.println("用户还未点过反对");
							out5.println(205);
							out5.flush();
						}else {
							System.out.println("用户已经点过反对");
							out5.println(305);
							out5.flush();
						}
						break;
						
					case '6':
						int check6 = 0;

						check6 = mysqlConnect.updateUserLikeCounter(str);
						// 修改成功
						if (check6 == 1)
							System.out.println("like域修改成功");
						else
							System.out.println("like域修改失败");
						break;

					case '7':
						int check7 = 0;

						check7 = mysqlConnect.updateUserHateCounter(str);
						// 修改成功
						if (check7 == 1)
							System.out.println("hate域修改成功");
						else
							System.out.println("hate域修改失败");
						break;

					case '8':
						int check8 = 0;
						PrintWriter out8 = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8")), true);

						check8 = mysqlConnect.updateUserPwd(str);
						// 修改成功
						if (check8 == 1) {
							System.out.println("pwd域修改成功");
							out8.println(204);
							out8.flush();
						} else {
							System.out.println("pwd域修改失败");
							out8.println(404);
							out8.flush();
						}
						break;
						
					case '9':
						int check9 = 0;
						check9 = mysqlConnect.updateUserState(str, 0);
						if(check9 == 1) 
							System.out.println("用户登出成功");
						else
							System.out.println("用户登出失败");

					}// end switch
				} // end try
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		// 连接数据库
		mysqlConnect.connect();
		mysqlConnect.createTables();
		new Thread(new ServerHandler()).start();
	}
}
