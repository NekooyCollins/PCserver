package server;

import java.sql.*;

public class mysqlConnect {
	private static final double Za = 1.96;

	// ���ݿ����Ӳ���
	public static Connection connect() {
		Connection con = null;

		try {
			// ����JDBC����
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				System.out.println("Success loading Mysql Driver!");
			} catch (Exception e) {
				System.out.println("Error loading Mysql Driver!");
				e.printStackTrace();
			}

			con = (Connection) DriverManager
					.getConnection("jdbc:mysql://127.0.0.1:3306/fireflyforest?serverTimezone=UTC", "root", "");
			con.setAutoCommit(true);
			System.out.println("Success connect Mysql server!");
		} catch (Exception e) {
			System.out.println("Connect failed!");
			e.printStackTrace();
		}
		return con;
	}

	public static void createTables() {

		Statement dictPstmt = null; // ����dict��
		Statement userPstmt = null; // ����user��
		Statement operationPstmt = null; // ����operation��

		Connection con = null;
		con = connect();
		// ����dict��
		try {
			dictPstmt = con.createStatement();
			dictPstmt.executeUpdate(
					"create table if not exists dict(user_name varchar(50), word varchar(20), interpret varchar(50), likes int, hates int, score double);");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Create dict table failed!");
			e.printStackTrace();
		}

		// ����users��
		try {
			userPstmt = con.createStatement();
			userPstmt.executeUpdate(
					"create table if not exists users(user_name varchar(50) primary key, password varchar(512), level int, state int);");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Create users table failed!");
			e.printStackTrace();
		}

		// ����operation��
		try {
			operationPstmt = con.createStatement();
			operationPstmt.executeUpdate(
					"create table if not exists operation(user_name varchar(50), word varchar(20), interpret varchar(50), likeflag int, hateflag int);");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Create operation table failed!");
			e.printStackTrace();
		}
	}

	// �ж��û��ύ�������Ƿ��Ѿ����ڣ�1���ڣ�-1������.�û��ύ����ʱʹ��
	public static int isParaExist(String wordInfo) throws SQLException {
		Connection con = null;
		String word = null, trans = null;
		String[] infoArray = new String[10];

		infoArray = wordInfo.split("\\|");
		word = infoArray[1];
		trans = infoArray[2];

		String sql = "select * from dict where word=? and interpret=?";

		con = connect();
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, word);
		pstmt.setString(2, trans);

		ResultSet result = pstmt.executeQuery();
		if (result.next()) {
			pstmt.close();
			return 1;
		} else {
			pstmt.close();
			return -1;
		}
	}

	// ��ѯ����
	public static String searchWord(String wordInfo) throws SQLException {
		Connection con = null;
		String searchWord = null, res = "";
		String userName = null, word = null, trans = null, likes = null, hates = null;
		String[] infoArray = new String[10];

		infoArray = wordInfo.split("\\|");
		searchWord = infoArray[1];

		String sql = "select * from dict where word=? order by score asc";

		con = connect();
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, searchWord);

		ResultSet result = pstmt.executeQuery();
		if (result.next()) {
			// �еĻ� �Ȼ�ȡ��һ����¼
			do {
				userName = result.getString("user_name");
				word = result.getString("word");
				trans = result.getString("interpret");
				likes = result.getString("likes");
				hates = result.getString("hates");

				res = userName + "|" + word + "|" + trans + "|" + likes + "|" + hates + ";" + res;
			} while (result.next());
		}

		pstmt.close();
		return res;
	}

	// �û��ύ���������dict����
	// �µ��������ʱ��������Դ��ͬ���ò�ͬ��score��ʼֵ����ɽΪ0.0001��ө���ٷ�Ϊ0.0003
	// ���ò�ͬscore�ĳ�ʼֵ��Ϊ�˵��û�ͶƱ��������С��ʱ���ṩһ�����Ŷ���Խϸߵ�����
	public static int insertToDict(String wordInfo) throws SQLException {
		String userName = null, word = null, interpret = null, likes = null, hates = null;
		double score = 0.0;
		Connection con = null;
		String[] infoArray = wordInfo.split("\\|");

		if (infoArray[0].equals("3")) {
			userName = infoArray[1];
			word = infoArray[2];
			interpret = infoArray[3];
			likes = infoArray[4];
			hates = infoArray[5];
		} else {
			userName = infoArray[0];
			word = infoArray[1];
			interpret = infoArray[2];
			likes = infoArray[3];
			hates = infoArray[4];
		}

		if (userName.equals("kingsoft"))
			score = 0.0001;
		else if (userName.equals("firefly office"))
			score = 0.0003;
		else
			score = 0.000;

		con = connect();
		String sql = "insert into dict values(?,?,?,?,?,?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, userName);
		pstmt.setString(2, word);
		pstmt.setString(3, interpret);
		pstmt.setInt(4, Integer.parseInt(likes));
		pstmt.setInt(5, Integer.parseInt(hates));
		pstmt.setDouble(6, score);

		int result = pstmt.executeUpdate();
		pstmt.close();
		return result;
	}

	// ע�����û�ʱ����,�����û�����Ϣ���뵽users����
	public static int insertToUsers(String info) throws SQLException {
		String userName = null, pwd = null;
		Connection con = null;
		String[] infoArray = new String[10];

		System.out.println(info);
		infoArray = info.split("\\|");
		userName = infoArray[1];
		pwd = infoArray[2];

		con = connect();
		String sql = "insert into users values(?,?,?,?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, userName);
		pstmt.setString(2, pwd);
		pstmt.setInt(3, 0);
		pstmt.setInt(4, 0);

		int result = pstmt.executeUpdate();
		pstmt.close();
		return result;
	}

	// �жϸ��û��Ƿ���ڣ�1���ڣ�-1������.�û�ע��ʱʹ��
	public static int isUsersNameExist(String userNameInfo) throws SQLException {
		Connection con = null;
		String userName = null;
		String[] infoArray = new String[10];

		infoArray = userNameInfo.split("\\|");
		System.out.println(infoArray[1]);
		userName = infoArray[1];

		String sql = "select * from users where user_name=?";

		con = connect();
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, userName);

		ResultSet result = pstmt.executeQuery();
		if (result.next()) {
			pstmt.close();
			return 1;
		} else {
			pstmt.close();
			return -1;
		}
	}

	// �жϸ��û��Ƿ���ڣ�1���ڣ�-1������.�û���¼ʱʹ��
	public static String isUsersNameAndPwdExist(String userLoginInfo) throws SQLException {
		Connection con = null;
		String userName = null, password = null;
		String name = null, level = null, response = null;
		String[] infoArray = new String[10];

		infoArray = userLoginInfo.split("\\|");
		userName = infoArray[1];
		password = infoArray[2];

		String sql = "select * from users where user_name=? and password=?";

		con = connect();
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, userName);
		pstmt.setString(2, password);

		ResultSet result = pstmt.executeQuery();

		if (result.next()) {
			do {
				name = result.getString("user_name");
				level = result.getString("level");
			} while (result.next());
			response = name + "|" + level;
		}
		System.out.println("response is:" + response);
		return response;
	}

	// �����û�״̬��1���ߣ�0������
	public static int updateUserState(String userInfo, int state) throws SQLException {
		Connection con = null;
		String userName = null;
		String[] infoArray = new String[10];

		infoArray = userInfo.split("\\|");
		userName = infoArray[1];

		System.out.println(state + userName);
		String sql = "update users set state='" + state + "' where user_name='" + userName + "'";
		con = connect();
		PreparedStatement pstmt = con.prepareStatement(sql);
		int result = pstmt.executeUpdate();
		pstmt.close();

		if (result > 0)
			return 1;
		else
			return -1;
	}

	// ��ȡ�û�����״̬,1��ʾ�Ѿ����ޣ�0��ʾδ����
	// δ��ѯ�����������µ�
	public static int checkLikeOperation(String checkInfo) throws SQLException {
		Connection con = null;
		String opUser = null, word = null, trans = null;
		String[] infoArray = new String[5];
		int res = 0;

		infoArray = checkInfo.split("\\|");
		opUser = infoArray[1];
		word = infoArray[2];
		trans = infoArray[3];

		String sql = "select * from operation where user_name=? and word=? and interpret=?";
		con = connect();
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, opUser);
		pstmt.setString(2, word);
		pstmt.setString(3, trans);

		ResultSet result = pstmt.executeQuery();
		if (result.next()) {
			System.out.println("��");
			do {
				res = result.getInt("likeflag");
			} while (result.next());

			if (res == 0)
				updateLikeOperation(opUser, word, trans, 1);
			else if (res == 1)
				updateLikeOperation(opUser, word, trans, 0);
		} else {
			System.out.println("��");
			insertUserOperation(opUser, word, trans, 1, 0);
		}

		System.out.println("like res:" + res);
		return res;
	}

	// ��ȡ�û�����״̬,1��ʾ�Ѿ����ԣ�0��ʾδ����
	// δ��ѯ�����������µ�
	public static int checkHateOperation(String checkInfo) throws SQLException {
		Connection con = null;
		String opUser = null, word = null, trans = null;
		String[] infoArray = new String[5];
		int res = 0;

		infoArray = checkInfo.split("\\|");
		opUser = infoArray[1];
		word = infoArray[2];
		trans = infoArray[3];

		String sql = "select * from operation where user_name=? and word=? and interpret=?";
		con = connect();
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, opUser);
		pstmt.setString(2, word);
		pstmt.setString(3, trans);

		ResultSet result = pstmt.executeQuery();
		if (result.next()) {
			do {
				res = result.getInt("hateflag");
			} while (result.next());

			if (res == 0)
				updateHateOperation(opUser, word, trans, 1);
			else if (res == 1)
				updateHateOperation(opUser, word, trans, 0);
		} else {
			insertUserOperation(opUser, word, trans, 0, 1);
		}

		System.out.println("hate res:" + res);
		return res;
	}

	// �����û�����
	public static void updateLikeOperation(String opUser, String word, String trans, int like) throws SQLException {
		Connection con = null;
		String sql = "update operation set likeflag=? where user_name=? and word=? and interpret=?";

		con = connect();
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setInt(1, like);
		pstmt.setString(2, opUser);
		pstmt.setString(3, word);
		pstmt.setString(4, trans);
		pstmt.executeUpdate();
		pstmt.close();
	}

	// �����û�����
	public static void updateHateOperation(String opUser, String word, String trans, int hate) throws SQLException {
		Connection con = null;
		String sql = "update operation set hateflag=? where user_name=? and word=? and interpret=?";

		con = connect();
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setInt(1, hate);
		pstmt.setString(2, opUser);
		pstmt.setString(3, word);
		pstmt.setString(4, trans);
		pstmt.executeUpdate();
		pstmt.close();
	}

	// ��һ�β����������û�������Ϣ
	public static void insertUserOperation(String opUser, String word, String trans, int like, int hate)
			throws SQLException {
		Connection con = null;
		String sql = "insert into operation values(?,?,?,?,?)";

		con = connect();
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, opUser);
		pstmt.setString(2, word);
		pstmt.setString(3, trans);
		pstmt.setInt(4, like);
		pstmt.setInt(5, hate);

		pstmt.executeUpdate();
		pstmt.close();
	}

	// ��ȡԭ����like���ֵ
	public static int findTheLikeCounter(String userName, String word, String trans) throws SQLException {
		Connection con = null;
		int likes = 0;
		String sql = "select * from dict where user_name=? and word=? and interpret=?";

		con = connect();
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, userName);
		pstmt.setString(2, word);
		pstmt.setString(3, trans);

		ResultSet result = pstmt.executeQuery();
		if (result.next()) {
			do {
				likes = result.getInt("likes");
			} while (result.next());
		}

		pstmt.close();
		System.out.println("��ǰ����ͬ��Ϊ��" + likes);
		return likes;
	}

	// ��ȡԭ����hate���ֵ
	public static int findTheHateCounter(String userName, String word, String trans) throws SQLException {
		Connection con = null;
		int hates = 0;
		String sql = "select * from dict where user_name=? and word=? and interpret=?";

		con = connect();
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, userName);
		pstmt.setString(2, word);
		pstmt.setString(3, trans);

		ResultSet result = pstmt.executeQuery();
		if (result.next()) {
			do {
				hates = result.getInt("hates");
			} while (result.next());
		}

		pstmt.close();
		System.out.println("��ǰ�ķ�����Ϊ��" + hates);
		return hates;
	}

	// ���������������
	public static int updateUserLikeCounter(String wordInfo) throws SQLException {
		Connection con = null;
		String userName = null, trans = null, word = null, likeCounter = null;
		int hateCounter = 0;
		int newLikeCounter = 0;
		double score = 0.0;
		String[] infoArray = new String[10];

		// ����ͻ��˴��������ַ���
		infoArray = wordInfo.split("\\|");
		userName = infoArray[1];
		word = infoArray[2];
		trans = infoArray[3];
		likeCounter = infoArray[4];

		// ��������µĵ���������ȡ��ǰ������
		newLikeCounter = findTheLikeCounter(userName, word, trans) + Integer.parseInt(likeCounter);
		hateCounter = findTheHateCounter(userName, word, trans);

		// ������ѷ�㷨�������һ�����͵ĵ÷�ֵ
		score = calculateScore(newLikeCounter, hateCounter);

		con = connect();

		String sql = "update dict set likes=? where user_name=? and word=? and interpret=?";
		String sql1 = "update dict set score=? where user_name=? and word=? and interpret=?";

		PreparedStatement pstmt = con.prepareStatement(sql);
		PreparedStatement pstmt1 = con.prepareStatement(sql1);

		pstmt.setInt(1, newLikeCounter);
		pstmt.setString(2, userName);
		pstmt.setString(3, word);
		pstmt.setString(4, trans);
		int result = pstmt.executeUpdate();

		pstmt1.setDouble(1, score);
		pstmt1.setString(2, userName);
		pstmt1.setString(3, word);
		pstmt1.setString(4, trans);
		int result1 = pstmt1.executeUpdate();

		pstmt.close();
		pstmt1.close();

		if ((result > 0) && (result1 > 0))
			return 1;
		else
			return -1;

	}

	// �����巴��������
	public static int updateUserHateCounter(String wordInfo) throws SQLException {
		Connection con = null;
		String userName = null, trans = null, word = null, hateCounter = null;
		int likeCounter = 0;
		int newHateCounter = 0;
		double score = 0.0;
		String[] infoArray = new String[10];

		infoArray = wordInfo.split("\\|");
		userName = infoArray[1];
		word = infoArray[2];
		trans = infoArray[3];
		hateCounter = infoArray[4];

		newHateCounter = findTheHateCounter(userName, word, trans) + Integer.parseInt(hateCounter);
		likeCounter = findTheLikeCounter(userName, word, trans);
		score = calculateScore(likeCounter, newHateCounter);

		con = connect();

		String sql = "update dict set hates=? where user_name=? and word=? and interpret=?";
		String sql1 = "update dict set score=? where user_name=? and word=? and interpret=?";

		PreparedStatement pstmt = con.prepareStatement(sql);
		PreparedStatement pstmt1 = con.prepareStatement(sql1);

		pstmt.setInt(1, newHateCounter);
		pstmt.setString(2, userName);
		pstmt.setString(3, word);
		pstmt.setString(4, trans);
		int result = pstmt.executeUpdate();

		pstmt1.setDouble(1, score);
		pstmt1.setString(2, userName);
		pstmt1.setString(3, word);
		pstmt1.setString(4, trans);
		int result1 = pstmt1.executeUpdate();

		pstmt.close();
		pstmt1.close();

		if ((result > 0) && (result1 > 0))
			return 1;
		else
			return -1;

	}

	// �û��������
	public static int updateUserPwd(String userInfo) throws SQLException {
		Connection con = null;
		String userName = null, pwd = null;
		String[] infoArray = new String[10];

		infoArray = userInfo.split("\\|");
		userName = infoArray[1];
		pwd = infoArray[2];

		String sql = "update users set password=? where user_name=?";
		con = connect();
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, pwd);
		pstmt.setString(2, userName);
		int result = pstmt.executeUpdate();
		pstmt.close();

		if (result > 0)
			return 1;
		else
			return -1;

	}

	// ����������۵ĵ÷�������Ϊ������׼��
	// ����ѷ�㷨
	public static double calculateScore(int like, int hate) {
		double score = 0.0;
		int n = 0;
		double p = 0.0, numerator = 0.0, denominator = 0.0;

		n = like + hate;
		if (n == 0) {
			System.out.println("score is:" + score);
			return score;
		} else {
			p = like / n;
			numerator = (p + (Za * Za) / (2 * n) - Za * Math.sqrt((p * (1 - p)) / n + (Za * Za) / (4 * n * n)));
			denominator = 1 + Za * Za / n;
			score = numerator / denominator;
			System.out.println("score is:" + score);

			return score;
		}
	}

	public void getUserLevel() throws SQLException {
		Connection con = null;
		String userName = null;

		String sql = "select user_name from users";

		con = connect();
		PreparedStatement pstmt = con.prepareStatement(sql);
		ResultSet result = pstmt.executeQuery();

		if (result.next()) {
			do {
				userName = result.getString("user_name");
				calculateUserLevel(userName);
			} while (result.next());
		} else
			return;

		pstmt.close();
	}

	public static void calculateUserLevel(String userName) throws SQLException {
		Connection con = null;
		double totalScores = 0.0;
		int count = 0, level = 0;
		String sql = "select scores from dict where user_name=?";

		con = connect();
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, userName);
		ResultSet result = pstmt.executeQuery();

		if (result.next()) {
			do {
				totalScores = result.getDouble("scores") + totalScores;
				count++;
			} while (result.next());
		} else
			return;

		totalScores = totalScores / count;
		if (totalScores < 0.1)
			level = 0;
		else if ((totalScores >= 0.2) && (totalScores < 0.3))
			level = 1;
		else if ((totalScores >= 0.3) && (totalScores < 0.4))
			level = 2;
		else if ((totalScores >= 0.4) && (totalScores < 0.5))
			level = 3;
		else if ((totalScores >= 0.5) && (totalScores < 0.6))
			level = 4;
		else if ((totalScores >= 0.6) && (totalScores < 0.8))
			level = 5;
		else if ((totalScores >= 0.8) && (totalScores < 0.9))
			level = 6;
		else if ((totalScores >= 0.9) && (totalScores < 1))
			level = 7;

		updateUserLevel(userName, level);
		pstmt.close();
	}

	public static void updateUserLevel(String userName, int level) throws SQLException {
		Connection con = null;
		String sql = "update users set level=? where user_name=?";

		con = connect();
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setInt(1, level);
		pstmt.setString(2, userName);
		int result = pstmt.executeUpdate();
		if (result == 1) {
			System.out.println("�û�" + userName + "�ȼ����³ɹ���");
		} else {
			System.out.println("�û�" + userName + "�ȼ�����ʧ�ܣ�");
		}
		pstmt.close();
	}
}
