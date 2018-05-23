package server;

import java.sql.*;

public class mysqlConnect {
	private static final double Za = 1.96;

	// 数据库连接操作
	public static Connection connect() {
		Connection con = null;

		try {
			// 加载JDBC驱动
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

		Statement dictPstmt = null; // 处理dict表
		Statement userPstmt = null; // 处理user表
		Statement operationPstmt = null; // 处理operation表

		Connection con = null;
		con = connect();
		// 创建dict表
		try {
			dictPstmt = con.createStatement();
			dictPstmt.executeUpdate(
					"create table if not exists dict(user_name varchar(50), word varchar(20), interpret varchar(50), likes int, hates int, score double);");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Create dict table failed!");
			e.printStackTrace();
		}

		// 创建users表
		try {
			userPstmt = con.createStatement();
			userPstmt.executeUpdate(
					"create table if not exists users(user_name varchar(50) primary key, password varchar(512), level int, state int);");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Create users table failed!");
			e.printStackTrace();
		}

		// 创建operation表
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

	// 判断用户提交的释义是否已经存在，1存在，-1不存在.用户提交释义时使用
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

	// 查询单词
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
			// 有的话 先获取第一条记录
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

	// 用户提交的释义存入dict表中
	// 新的释义插入时，根据来源不同设置不同的score初始值，金山为0.0001，萤火虫官方为0.0003
	// 设置不同score的初始值是为了当用户投票样本数很小的时候提供一个可信度相对较高的排名
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

	// 注册新用户时调用,将新用户的信息插入到users表中
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

	// 判断该用户是否存在，1存在，-1不存在.用户注册时使用
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

	// 判断该用户是否存在，1存在，-1不存在.用户登录时使用
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

	// 更新用户状态，1在线，0不在线
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

	// 获取用户点赞状态,1表示已经点赞，0表示未点赞
	// 未查询到结果则插入新的
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
			System.out.println("有");
			do {
				res = result.getInt("likeflag");
			} while (result.next());

			if (res == 0)
				updateLikeOperation(opUser, word, trans, 1);
			else if (res == 1)
				updateLikeOperation(opUser, word, trans, 0);
		} else {
			System.out.println("无");
			insertUserOperation(opUser, word, trans, 1, 0);
		}

		System.out.println("like res:" + res);
		return res;
	}

	// 获取用户反对状态,1表示已经反对，0表示未反对
	// 未查询到结果则插入新的
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

	// 更新用户操作
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

	// 更新用户操作
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

	// 第一次操作，插入用户操作信息
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

	// 获取原来的like域的值
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
		System.out.println("当前的赞同数为：" + likes);
		return likes;
	}

	// 获取原来的hate域的值
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
		System.out.println("当前的反对数为：" + hates);
		return hates;
	}

	// 该释义点赞数更新
	public static int updateUserLikeCounter(String wordInfo) throws SQLException {
		Connection con = null;
		String userName = null, trans = null, word = null, likeCounter = null;
		int hateCounter = 0;
		int newLikeCounter = 0;
		double score = 0.0;
		String[] infoArray = new String[10];

		// 处理客户端传过来的字符串
		infoArray = wordInfo.split("\\|");
		userName = infoArray[1];
		word = infoArray[2];
		trans = infoArray[3];
		likeCounter = infoArray[4];

		// 计算出最新的点赞数，获取当前反对数
		newLikeCounter = findTheLikeCounter(userName, word, trans) + Integer.parseInt(likeCounter);
		hateCounter = findTheHateCounter(userName, word, trans);

		// 用威尔逊算法计算出这一条解释的得分值
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

	// 该释义反对数更新
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

	// 用户密码更新
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

	// 计算各个评论的得分数量，为排序做准备
	// 威尔逊算法
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
			System.out.println("用户" + userName + "等级更新成功！");
		} else {
			System.out.println("用户" + userName + "等级更新失败！");
		}
		pstmt.close();
	}
}
