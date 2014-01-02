package com.chp.training;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.postgresql.core.ResultHandler;
import org.postgresql.ds.PGSimpleDataSource;

public class DataBaseFunctions {

	private static JSONParser jsonParser = new JSONParser();
	static String URL = "localhost";
	static String DATABASE = "learning2";
	static String PORT = "5433";
	static String USER = "postgres";
	static String PASSWORD = "postgres";

	private static PGSimpleDataSource pgSimpleDataSourceWeb;

	static PreparedStatement insertQuestionBoxStatement = null;
	static PreparedStatement insertTopicStatement = null;
	static PreparedStatement insertTreatmentStatement = null;
	static PreparedStatement insertPathStatement = null;
	static PreparedStatement isPathEndStatement = null;
	static PreparedStatement getNextQuestionBoxStatenment = null;
	static PreparedStatement getTopicsStatenment = null;

	/**
	 * 
	 * @return A connection to the database, currently having all rights.
	 * @throws SQLException
	 */
	public static Connection getWebConnection() throws SQLException {
		Connection con = null;
		try {
			if (pgSimpleDataSourceWeb == null) {
				pgSimpleDataSourceWeb = new PGSimpleDataSource();
				pgSimpleDataSourceWeb.setServerName(URL);
				pgSimpleDataSourceWeb.setPortNumber(Integer.valueOf(PORT));
				pgSimpleDataSourceWeb.setDatabaseName(DATABASE);
				pgSimpleDataSourceWeb.setUser(USER);
				pgSimpleDataSourceWeb.setPassword(PASSWORD);

			}
			con = pgSimpleDataSourceWeb.getConnection();
			con.setAutoCommit(true);
			
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Could not properly build a connection to Database.\n"
							+ "Function: getWebConnection()\n"
							+ "Details: %s\n"
							+ "pgSimpleDataSourceWeb == null: %B\n"
							+ "con == null: %B", e.getMessage(),
					pgSimpleDataSourceWeb == null, con == null));
		}
		try {
			insertTopicStatement = con
					.prepareStatement(DatabaseStatements.INSERT_TOPIC);
			insertQuestionBoxStatement = con
					.prepareStatement(DatabaseStatements.INSERT_QUESTION_BOX);
			insertTreatmentStatement = con
					.prepareStatement(DatabaseStatements.INSERT_TREATMENT);
			insertPathStatement = con
					.prepareStatement(DatabaseStatements.INSERT_PATH);
			isPathEndStatement = con
					.prepareStatement(DatabaseStatements.IS_PATH_END);
			getNextQuestionBoxStatenment = con
					.prepareStatement(DatabaseStatements.GET_NEXT_QUESTION_BOX);
			getTopicsStatenment = con
					.prepareStatement(DatabaseStatements.GET_TOPICS);
			return con;
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Could not prepare the statements.\n"
							+ "Function: getWebConnection()\n" + "Details: %s",
					e.getMessage()));
		}
	}
	
	public static JSONObject getNextCategories(Connection con, JSONObject parameters) {
		String path = parameters.get("path").toString();
		PreparedStatement isEndStatement = null;
		try {
			isEndStatement = con.prepareStatement("SELECT material FROM materials m WHERE (?)::ltree <@ m.category_path");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			isEndStatement.setString(1, path);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet rsEnd;
		JSONObject resultObject = new JSONObject();
		try {
			rsEnd = isEndStatement.executeQuery();
			if (rsEnd.next()) {
				String material = rsEnd.getString(1);
				resultObject.put("material", material);
				return resultObject;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		PreparedStatement nextCategoryStatement = null;
		
		try {
			nextCategoryStatement = con.prepareStatement("WITH paras AS (SELECT (?)::ltree as path) SELECT part as id, title FROM categories c, (SELECT DISTINCT subpath(category_path,0,nlevel(paras.path)+1) as part FROM materials m,paras WHERE paras.path @> m.category_path) p WHERE ('*.'||(c.id::text))::lquery ~ part");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			nextCategoryStatement.setString(1, path);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			ResultSet rs = nextCategoryStatement.executeQuery();
			while (rs.next()) {
				String id = rs.getString(1);
				String title = rs.getString(2);
				resultObject.put(id, title);
			}
			return resultObject;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static JSONObject getTopCategories(Connection con) {
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement("SELECT * FROM categories c WHERE ((c.id::text)||'.*')::lquery ~ ANY (SELECT category_path FROM materials)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ResultSet rs;
		JSONObject resultObject = new JSONObject();
		try {
			rs = pstmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				resultObject.put(id, name);
				System.out.println("name: "+name);
				System.out.println(resultObject.get(id));
			}
			return resultObject;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	

	public static JSONArray getTopics(Connection con) throws SQLException {
		JSONArray resultArray = new JSONArray();
		try {
			ResultSet rs = getTopicsStatenment.executeQuery();
			while (rs.next()) {
				String s = rs.getString(1);

				JSONObject obj = null;
				try {
					obj = (JSONObject) jsonParser.parse(s);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				resultArray.add(obj);
			}
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Execution of Statement failed.\n"
							+ "Function: insertNewQuestionPath()\n"
							+ "Statement: %s\n" + "Details: %s",
					insertTopicStatement.toString(), e.getMessage()));
		}
		return resultArray;
	}

	/**
	 * 
	 * @param con
	 * @param parameters
	 *            A questionBox as a JSONObject which has the following
	 *            parameters:<br>
	 *            topic_title : String,<br>
	 *            topic_description : String,<br>
	 *            questions : JSONArray "Questions" (see below) <br>
	 *            Additionally key-value pairs as the following should be
	 *            provided. The value of these decide whether to give a direct
	 *            treatment (String) or another QuestionBox depending on how
	 *            many questions have been answered with yes (key):<br>
	 *            (int) : String OR (QuestionBox)<br>
	 * <br>
	 *            If you give another QuestionBox as a value (which would end in
	 *            some kind of recursion, this Questionbox does NOT need to
	 *            provide topic_title or topic_description. <br>
	 * <br>
	 *            JSONArray "Questions" contains JSONObjects as the following:<br>
	 *            question : String <br>
	 *            details : String <br>
	 * @throws SQLException
	 * 
	 */
	public static void insertNewQuestionPath(Connection con,
			JSONObject parameters) throws SQLException {
		JSONObject first = (JSONObject) parameters.clone();
		ArrayDeque<JSONObject> jobQueue = new ArrayDeque<JSONObject>();
		first.put("path", "");
		try {

			String topic_title = first.remove("topic_title").toString();
			String topic_description = first.remove("topic_description")
					.toString();
			insertTopicStatement.setString(1, topic_title);
			insertTopicStatement.setString(2, topic_description);
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Adding parameters to the statement failed\n"
							+ "Function: insertNewQuestionPath()\n"
							+ "Statement: %s\n" + "Parameters: %s\n"
							+ "Details: %s", insertTopicStatement.toString(),
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}
		ResultSet topicResult;
		try {
			topicResult = insertTopicStatement.executeQuery();
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Execution of Statement failed.\n"
							+ "Function: insertNewQuestionPath()\n"
							+ "Statement: %s\n" + "Parameters: %s\n"
							+ "Details: %s", insertTopicStatement.toString(),
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}
		if (!topicResult.next()) {
			throw new SQLException(String.format(
					"Statement did not return expected Result.\n"
							+ "Function: insertNewQuestionPath()\n"
							+ "Statement: %s\n" + "Parameters: %s\n",
					insertTopicStatement.toString(),
					Helper.niceJsonPrint(parameters, "")));
		}
		int topic = topicResult.getInt(1);
		jobQueue.add(first);
		while (!jobQueue.isEmpty()) {
			JSONObject questionBoxJob = jobQueue.pop();
			System.out.println("Found Box");
			String path = questionBoxJob.remove("path").toString();
			System.out.println("Path: " + path);
			JSONArray jsonQuestions = (JSONArray) questionBoxJob
					.remove("questions");
			System.out.println("Questions: " + jsonQuestions.toString());
			String description = questionBoxJob.remove("description")
					.toString();
			int questionNum = jsonQuestions.size();
			String[] questionStrings = new String[questionNum];
			int count = 0;
			for (Object questionObject : jsonQuestions) {
				JSONObject jsonQuestion = (JSONObject) questionObject;
				String question = jsonQuestion.get("question").toString();
				String details = jsonQuestion.get("details").toString();
				String questionString = "(" + question + "," + details + ")";
				questionStrings[count++] = questionString;
				System.out.println("one question String: " + questionString);
			}
			try {
				Array questionArray = con.createArrayOf("question",
						questionStrings);
				insertQuestionBoxStatement.setString(1, description);
				insertQuestionBoxStatement.setArray(2, questionArray);

			} catch (SQLException e) {
				throw new SQLException(String.format(
						"Adding parameters to the statement failed\n"
								+ "Statement: %s\n"
								+ "Function: insertNewQuestionPath()\n"
								+ "Parameters: %s\n" + "Details: %s",
						insertQuestionBoxStatement.toString(),
						Helper.niceJsonPrint(parameters, ""), e.getMessage()));
			}
			ResultSet rs;
			try {
				rs = insertQuestionBoxStatement.executeQuery();
				if (!rs.next())
					throw new SQLException(String.format(
							"Statement did not return expected Result.\n"
									+ "Function: insertNewQuestionPath()\n"
									+ "Statement: %s\n" + "Parameters: %s\n",
							insertTopicStatement.toString(),
							Helper.niceJsonPrint(parameters, "")));
			} catch (SQLException e) {
				throw new SQLException(String.format(
						"Execution of Statement failed.\n"
								+ "Function: insertNewQuestionPath()\n"
								+ "Statement: %s\n" + "Parameters: %s\n"
								+ "Details: %s",
						insertQuestionBoxStatement.toString(),
						Helper.niceJsonPrint(parameters, ""), e.getMessage()));
			}

			int questionBoxID = rs.getInt(1);
			System.out.println("ID of box: " + questionBoxID);

			Set<Object> keySet = questionBoxJob.keySet();

			for (Object keyObject : keySet) {
				System.out.println("Next key: " + keyObject.toString());

				String keyString = keyObject.toString();
				if (!keyString.matches("[0-9]*")) {
					continue;

				}
				Object valueObject = questionBoxJob.get(keyObject);

				String newPath = path + (path.equals("") ? "" : ".")
						+ String.valueOf(questionBoxID) + "_" + keyString;
				System.out.println(String.format("Old path: %s\nNew path: %s",
						path, newPath));
				if (valueObject instanceof JSONObject) {
					System.out.println("Found inside box");
					JSONObject nextJob = (JSONObject) valueObject;
					nextJob.put("path", newPath);
					jobQueue.add(nextJob);
				} else if (valueObject instanceof String) {
					String treatment = valueObject.toString();
					System.out
							.println("Just found inside string: " + treatment);
					try {
						insertTreatmentStatement.setString(1, treatment);
					} catch (SQLException e) {
						throw new SQLException(String.format(
								"Adding parameters to the statement failed\n"
										+ "Statement: %s\n"
										+ "Function: insertNewQuestionPath()\n"
										+ "Parameters: %s\n" + "Details: %s",
								insertTreatmentStatement.toString(),
								Helper.niceJsonPrint(parameters, ""),
								e.getMessage()));
					}
					ResultSet rs2;
					try {
						rs2 = insertTreatmentStatement.executeQuery();
						if (!rs2.next())
							throw new SQLException(
									String.format(
											"Statement did not return expected Result.\n"
													+ "Function: insertNewQuestionPath()\n"
													+ "Statement: %s\n"
													+ "Parameters: %s\n",
											insertTreatmentStatement.toString(),
											Helper.niceJsonPrint(parameters, "")));

					} catch (SQLException e) {
						throw new SQLException(String.format(
								"Execution of Statement failed.\n"
										+ "Function: insertNewQuestionPath()\n"
										+ "Statement: %s\n"
										+ "Parameters: %s\n" + "Details: %s",
								insertTreatmentStatement.toString(),
								Helper.niceJsonPrint(parameters, ""),
								e.getMessage()));
					}
					int treatmentID = rs2.getInt(1);

					PreparedStatement pathInsertStatement = con
							.prepareStatement("INSERT INTO paths (id,path,treatment,topic) VALUES (default,(?)::ltree,?,?)");
					try {
						pathInsertStatement.setString(1, newPath);
						pathInsertStatement.setInt(2, treatmentID);
						pathInsertStatement.setInt(3, topic);
					} catch (SQLException e) {
						throw new SQLException(String.format(
								"Adding parameters to the statement failed\n"
										+ "Statement: %s\n"
										+ "Function: insertNewQuestionPath()\n"
										+ "Parameters: %s\n" + "Details: %s",
								pathInsertStatement.toString(),
								Helper.niceJsonPrint(parameters, ""),
								e.getMessage()));
					}
					try {
						System.out.println(pathInsertStatement.toString());
						pathInsertStatement.executeUpdate();
					} catch (SQLException e) {
						throw new SQLException(String.format(
								"Execution of Statement failed.\n"
										+ "Function: insertNewQuestionPath()\n"
										+ "Statement: %s\n"
										+ "Parameters: %s\n" + "Details: %s",
								insertTreatmentStatement.toString(),
								Helper.niceJsonPrint(parameters, ""),
								e.getMessage()));
					}
				} else
					System.out.println("Found strange thing inside");

			}

		}
	}

	/**
	 * 
	 * @param con
	 * @param parameters
	 *            JSON Object with the following parameters:<br>
	 *            topic : (int)<br>
	 *            yes_count : (int)
	 *            OPTIONAL:
	 *            session : (String, see below, obviously not needed for first QuestionBox)<br>
	 * @return One of the following JSON Objects:<br>
	 *         {<br>
	 *         result_type : "treatment",<br>
	 *         treatment : (String)<br>
	 *         }<br>
	 * <br>
	 *         {<br>
	 *         result_type : "question_box",<br>
	 *         session : (String) <- this has to be sent back<br>
	 *         question_box:<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;description : (String),<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;questions : [<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp
	 *         ;&nbsp;{<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp
	 *         ;&nbsp;question : (String),<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp
	 *         ;&nbsp;details : (String)<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp
	 *         ;&nbsp;}<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp
	 *         ;&nbsp;...<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp
	 *         ;&nbsp;]<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br>
	 *         }
	 * @throws SQLException
	 */
	public static JSONObject getNextQuestionBox(Connection con,
			JSONObject parameters) throws SQLException {
		String topicS = parameters.get("topic").toString();
		Object pathO = parameters.get("session");
		String path = pathO==null?"":pathO.toString();
		String yesCount = parameters.get("yes_count").toString();
		path += yesCount;

		int topic = Integer.valueOf(topicS);
		JSONObject obj = null;
		try {
			isPathEndStatement.setInt(1, topic);
			isPathEndStatement.setString(2, path);

		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Adding parameters to the statement failed\n"
							+ "Function: insertNewQuestionPath()\n"
							+ "Statement: %s\n" + "Parameters: %s\n"
							+ "Details: %s", isPathEndStatement.toString(),
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}
		JSONObject result = new JSONObject();
		ResultSet rs;
		try {
			rs = isPathEndStatement.executeQuery();
			if (rs.next()) {
				String treatment = rs.getString(1);
				result.put("result_type", "treatment");
				result.put("treatment", treatment);
				return result;
			}
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Execution of Statement failed.\n"
							+ "Function: insertNewQuestionPath()\n"
							+ "Statement: %s\n" + "Parameters: %s\n"
							+ "Details: %s", isPathEndStatement.toString(),
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}

		try {
			getNextQuestionBoxStatenment.setInt(1, topic);
			getNextQuestionBoxStatenment.setString(2, path);

		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Adding parameters to the statement failed\n"
							+ "Function: insertNewQuestionPath()\n"
							+ "Statement: %s\n" + "Parameters: %s\n"
							+ "Details: %s",
					getNextQuestionBoxStatenment.toString(),
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}
		ResultSet resultBox;
		try {
			resultBox = getNextQuestionBoxStatenment.executeQuery();
			if (!resultBox.next()) {
				throw new SQLException(String.format(
						"Statement did not return expected Result.\n"
								+ "Function: insertNewQuestionPath()\n"
								+ "Statement: %s\n" + "Parameters: %s\n",
						getNextQuestionBoxStatenment.toString(),
						Helper.niceJsonPrint(parameters, "")));
			}
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Execution of Statement failed.\n"
							+ "Function: insertNewQuestionPath()\n"
							+ "Statement: %s\n" + "Parameters: %s\n"
							+ "Details: %s",
					getNextQuestionBoxStatenment.toString(),
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}

		String s = resultBox.getString(1);
		JSONObject jsonOb = null;
		try {
			jsonOb = (JSONObject) jsonParser.parse(s);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		result.put("result_type", "question_box");
		result.put("session", path + "." + jsonOb.remove("id").toString() + "_");
		result.put("question_box", jsonOb);

		if (resultBox.next()) {
			System.out
					.println("There are more possible boxes (strange thing).");
		}

		return result;
	}

	private static JSONArray getSampleQuestionArray(String[] questions) {
		JSONArray output = new JSONArray();
		for (String question : questions) {
			JSONObject jsonQuestion = new JSONObject();
			jsonQuestion.put("question", question);
			jsonQuestion.put("details",
					"<Here we could have some Details for that question>");
			output.add(jsonQuestion);
		}
		return output;
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// JSONObject questionBox1 = new JSONObject();
		// JSONObject questionBox2 = new JSONObject();
		// JSONObject questionBox3 = new JSONObject();
		// JSONObject questionBox4 = new JSONObject();
		//
		// questionBox4.put("questions",
		// getSampleQuestionArray(new String[] { "Question 1" }));
		// questionBox4
		// .put("description",
		// "This question box appeared after giving no yes at all in 2 questionboxes.");
		// questionBox4.put("0", "You just say no to everything");
		// questionBox4.put("1", "Finally a yes after all these no's");
		//
		// questionBox2.put("questions",
		// getSampleQuestionArray(new String[] { "Question 1" }));
		// questionBox2.put("description",
		// "This question box appeared after giving 1 \"yes\".");
		// questionBox2.put("0", "1 Yes at all is really enough..");
		// questionBox2.put("1", "Always 1 yes per question, good");
		//
		// questionBox3.put("questions",
		// getSampleQuestionArray(new String[] { "Question 1" }));
		// questionBox3
		// .put("description",
		// "This question box appeared after giving no yes at all in 1 questionsbox.");
		// questionBox3.put("0", questionBox4);
		// questionBox3.put("1", "Yay, at least 2nd box got a yes.");
		//
		// questionBox1.put("questions", getSampleQuestionArray(new String[] {
		// "Question 1", "Question 2" }));
		// questionBox1.put("description", "This is the very first question");
		// questionBox1.put("0", questionBox3);
		// questionBox1.put("1", questionBox2);
		// questionBox1.put("2", "Don't say yes to everything");
		// questionBox1.put("topic_title", "Peter's Area");
		// questionBox1
		// .put("topic_description", "This topic is just for testing.");
		//
		Connection con;
		try {
			con = getWebConnection();
			// insertNewQuestionPath(con, questionBox1);
			JSONObject parameters = new JSONObject();
			parameters.put("topic", 1);
			parameters.put("session", "1_0.3_");
			parameters.put("yes_count", "0");
			JSONArray object = getTopics(con);
			System.out.println(Helper.niceJsonPrint(object, ""));
			JSONObject ob = getTopCategories(con);
			System.out.println(ob.toJSONString());
			JSONObject in1 = new JSONObject();
			in1.put("path", 5);
			JSONObject res1 = getNextCategories(con, in1);
			System.out.println(res1.toJSONString());
			
			JSONObject in2 = new JSONObject();
			in2.put("path", "5.10");
			JSONObject res2 = getNextCategories(con, in2);
			System.out.println(res2.toJSONString());

			JSONObject in3 = new JSONObject();
			in3.put("path", "5.10.13");
			JSONObject res3 = getNextCategories(con, in3);
			System.out.println(res3.toJSONString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
