package com.chp.training;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
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
	static PreparedStatement insertActionStatement = null;
	static PreparedStatement isPathEndStatement = null;
	static PreparedStatement getNextQuestionBoxPathStatenment = null;
	static PreparedStatement getNextQuestionBoxNoPathStatenment = null;
	static PreparedStatement getTopicsStatenment = null;
	static Connection connection = null;

	/**
	 * 
	 * @return A connection to the database, currently having all rights.
	 * @throws SQLException
	 */
	public static Connection getWebConnection() throws SQLException {
		if (pgSimpleDataSourceWeb == null) {
			pgSimpleDataSourceWeb = new PGSimpleDataSource();
			pgSimpleDataSourceWeb.setServerName(URL);
			pgSimpleDataSourceWeb.setPortNumber(Integer.valueOf(PORT));
			pgSimpleDataSourceWeb.setDatabaseName(DATABASE);
			pgSimpleDataSourceWeb.setUser(USER);
			pgSimpleDataSourceWeb.setPassword(PASSWORD);

		}
		if (connection == null) {
			try {
				connection = pgSimpleDataSourceWeb.getConnection();
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				throw new SQLException(String.format(
						"Could not properly build a connection to Database.\n"
								+ "Function: getWebConnection()\n"
								+ "Details: %s\n"
								+ "pgSimpleDataSourceWeb == null: %B\n"
								+ "con == null: %B", e.getMessage(),
						pgSimpleDataSourceWeb == null, connection == null));
			}
			try {
				insertTopicStatement = connection
						.prepareStatement(DatabaseStatements.INSERT_TOPIC);
				insertQuestionBoxStatement = connection
						.prepareStatement(DatabaseStatements.INSERT_QUESTION_BOX);
				insertTreatmentStatement = connection
						.prepareStatement(DatabaseStatements.INSERT_TREATMENT);
				insertPathStatement = connection
						.prepareStatement(DatabaseStatements.INSERT_PATH);
				insertActionStatement = connection
						.prepareStatement(DatabaseStatements.INSERT_ACTION);
				isPathEndStatement = connection
						.prepareStatement(DatabaseStatements.IS_PATH_END);
				getNextQuestionBoxPathStatenment = connection
						.prepareStatement(DatabaseStatements.GET_NEXT_QUESTION_BOX_PATH);
				getNextQuestionBoxNoPathStatenment = connection
						.prepareStatement(DatabaseStatements.GET_NEXT_QUESTION_BOX_NO_PATH);
				getTopicsStatenment = connection
						.prepareStatement(DatabaseStatements.GET_TOPICS);
			} catch (SQLException e) {
				throw new SQLException(String.format(
						"Could not prepare the statements.\n"
								+ "Function: getWebConnection()\n"
								+ "Details: %s", e.getMessage()));
			}
		}
		return connection;

	}

	public static JSONObject getNextCategories(Connection con,
			JSONObject parameters) {
		String path = parameters.get("path").toString();
		PreparedStatement isEndStatement = null;
		try {
			isEndStatement = con
					.prepareStatement("SELECT material FROM materials m WHERE (?)::ltree <@ m.category_path");
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
			nextCategoryStatement = con
					.prepareStatement("WITH paras AS (SELECT (?)::ltree as path) SELECT part as id, title FROM categories c, (SELECT DISTINCT subpath(category_path,0,nlevel(paras.path)+1) as part FROM materials m,paras WHERE paras.path @> m.category_path) p WHERE ('*.'||(c.id::text))::lquery ~ part");
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
			pstmt = con
					.prepareStatement("SELECT * FROM categories c WHERE ((c.id::text)||'.*')::lquery ~ ANY (SELECT category_path FROM materials)");
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
	 *            (int) : (ActionObject)<br>
	 * <br>
	 *            An ActionObject can be one of the following JSONObjects: {
	 *            action: treatment treatment: { } }
	 * 
	 * 
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
	@SuppressWarnings("unchecked")
	public static void insertNewQuestionPath(Connection con,
			JSONObject parameters) throws SQLException {
		boolean firstB = true;
		JSONObject first = (JSONObject) parameters.clone();
		ArrayDeque<JSONObject> jobQueue = new ArrayDeque<JSONObject>();
		try {

			JSONObject topic = (JSONObject) first.get("topic");
			String topic_title = topic.get("title").toString();
			Object topic_descriptionOb = topic.get("description");
			String topic_description = topic_descriptionOb == null ? ""
					: topic_descriptionOb.toString();
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
		JSONObject firstBox = (JSONObject) first.get("questionbox");
		jobQueue.add(firstBox);
		while (!jobQueue.isEmpty()) {
			JSONObject questionBoxJob = jobQueue.pop();
			System.out.println("Found Box");

			JSONArray jsonQuestions = (JSONArray) questionBoxJob
					.remove("questions");
			System.out.println("Questions: " + jsonQuestions.toString());
			Object descriptionO = questionBoxJob.remove("description");
			String description = descriptionO == null ? "" : descriptionO
					.toString();
			int questionNum = jsonQuestions.size();
			String[] questionStrings = new String[questionNum];
			int count = 0;
			for (Object questionObject : jsonQuestions) {
				JSONObject jsonQuestion = (JSONObject) questionObject;
				String question = jsonQuestion.get("question").toString();
				Object questionDetO = jsonQuestion.get("details");
				String questionDet = questionDetO==null?"":questionDetO.toString();
				String questionString = "(" + question + "," + questionDet + ")";
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

			if (!firstB) {
				JSONObject source = (JSONObject) questionBoxJob.get("source");
				String source_yes_list = source.get("source_yes_list")
						.toString();
				String[] source_yes_Strings = source_yes_list.split(",");
				Integer source_id = Integer.valueOf(source.get("source_id")
						.toString());

				for (String source_yes_String : source_yes_Strings) {
					insertActionStatement.setInt(1, questionBoxID);
					insertActionStatement.setInt(2,
							Integer.valueOf(source_yes_String));
					insertActionStatement.setString(3, "next_box");
					insertActionStatement.setInt(4, source_id);
					insertActionStatement.addBatch();
				}
				insertActionStatement.executeBatch();
			} else
				firstB = false;

			System.out.println("ID of box: " + questionBoxID);

			Set<Object> keySet = questionBoxJob.keySet();

			for (Object keyObject : keySet) {

				String keyString = keyObject.toString();
				System.out.println("Next key: " + keyString);

				if (!keyString.matches("[0-9]+(,[0-9]+)*"))
					continue;

				JSONObject valueObject = (JSONObject) questionBoxJob
						.get(keyObject);

				String action = valueObject.get("action").toString();

				String[] keys = keyString.split(",");

				if ("next_box".equals(action)) {
					JSONObject source_obj = new JSONObject();
					source_obj.put("source_id", questionBoxID);
					source_obj.put("source_yes_list", keyString);

					JSONObject next_job = (JSONObject) valueObject
							.get("questionbox");
					next_job.put("source", source_obj);
					jobQueue.add(next_job);
					continue;
				}

				for (String key : keys) {
					if ("change_topic".equals(action)) {

					} else if ("next_topic".equals(action)) {
						insertActionStatement.setInt(1, questionBoxID);
						insertActionStatement.setInt(2, Integer.valueOf(key));
						insertActionStatement.setString(3, "next_topic");
						insertActionStatement.setInt(4, topic);
						insertActionStatement.execute();
					} else if ("treatment".equals(action)) {
						JSONObject treatmentObject = (JSONObject) valueObject
								.get("treatment");
						// String treatment_title =
						// treatmentObject.get("title").toString();
						String treatment_description = treatmentObject.get(
								"description").toString();

						try {
							// insertTreatmentStatement.setString(1,
							// treatment_title);
							insertTreatmentStatement.setString(1,
									treatment_description);
						} catch (SQLException e) {
							throw new SQLException(
									String.format(
											"Adding parameters to the statement failed\n"
													+ "Statement: %s\n"
													+ "Function: insertNewQuestionPath()\n"
													+ "Parameters: %s\n"
													+ "Details: %s",
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
												insertTreatmentStatement
														.toString(), Helper
														.niceJsonPrint(
																parameters, "")));

						} catch (SQLException e) {
							throw new SQLException(
									String.format(
											"Execution of Statement failed.\n"
													+ "Function: insertNewQuestionPath()\n"
													+ "Statement: %s\n"
													+ "Parameters: %s\n"
													+ "Details: %s",
											insertTreatmentStatement.toString(),
											Helper.niceJsonPrint(parameters, ""),
											e.getMessage()));
						}
						int treatmentID = rs2.getInt(1);

						insertActionStatement.setInt(1, questionBoxID);
						insertActionStatement.setInt(2, Integer.valueOf(key));
						insertActionStatement.setString(3, "treatment");
						insertActionStatement.setInt(4, treatmentID);
						insertActionStatement.execute();

					}
				}
			}
		}

	}

	/**
	 * 
	 * @param con
	 * @param parameters
	 *            JSON Object with the following parameters:<br>
	 *            topic : (int)<br>
	 *            CONDITIONAL MANDATORY: yes_count : (int, mandatory if and only
	 *            if session is provided) OPTIONAL: session : (String, see
	 *            below, obviously not needed for first QuestionBox)<br>
	 * @return One of the following JSON Objects:<br>
	 *         {<br>
	 *         action : "treatment",<br>
	 *         treatment:<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;id : (int), description :
	 *         (String),<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br>
	 *         }<br>
	 * <br>
	 *         {<br>
	 *         action : "change_topic",<br>
	 *         topic:<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;id : (int), title : (String),
	 *         description : (String),<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br>
	 *         question_box:<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;id : (int), description :
	 *         (String),<br>
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
	 *         }<br>
	 * <br>
	 *         {<br>
	 *         action : "next_box",<br>
	 *         question_box:<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{<br>
	 *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;id : (int), description :
	 *         (String),<br>
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
	 * @throws ParseException
	 */
	public static JSONObject getNextAction(Connection con, JSONObject parameters)
			throws SQLException, ParseException {
		String questionboxS = parameters.get("questionbox").toString();
		Integer questionBox = Integer.valueOf(questionboxS);
		String yesCountS = parameters.get("yes_count").toString();
		Integer yesCount = Integer.valueOf(yesCountS);

		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement("SELECT proceed_result(?,?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			pstmt.setInt(1, questionBox);
			pstmt.setInt(2, yesCount);
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Adding parameters to the statement failed\n"
							+ "Statement: %s\n"
							+ "Function: getNextQuestionBox()\n"
							+ "Parameters: %s\n" + "Details: %s",
					pstmt.toString(), Helper.niceJsonPrint(parameters, ""),
					e.getMessage()));
		}

		ResultSet rs;
		try {
			rs = pstmt.executeQuery();
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Execution of Statement failed.\n"
							+ "Function: getNextQuestionBox()\n"
							+ "Statement: %s\n" + "Parameters: %s\n"
							+ "Details: %s", pstmt.toString(),
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}
		if (!rs.next()) {
			return null;
		}
		String jsonString = rs.getString(1);
		JSONObject json = (JSONObject) jsonParser.parse(jsonString);
		return json;
	}

	public static JSONObject getFirstQuestionBox(Connection con,
			JSONObject parameters) throws SQLException, ParseException {
		String topicS = parameters.get("topic").toString();
		Integer topic = Integer.valueOf(topicS);
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement("SELECT get_first_box(?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			pstmt.setInt(1, topic);
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Adding parameters to the statement failed\n"
							+ "Statement: %s\n"
							+ "Function: getFirstQuestionBox()\n"
							+ "Parameters: %s\n" + "Details: %s",
					pstmt.toString(), Helper.niceJsonPrint(parameters, ""),
					e.getMessage()));
		}
		ResultSet rs;
		try {
			rs = pstmt.executeQuery();
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Execution of Statement failed.\n"
							+ "Function: getFirstQuestionBox()\n"
							+ "Statement: %s\n" + "Parameters: %s\n"
							+ "Details: %s", pstmt.toString(),
					Helper.niceJsonPrint(parameters, ""), e.getMessage()));
		}
		if (!rs.next()) {
			return null;
		}
		String jsonString = rs.getString(1);
		JSONObject json = (JSONObject) jsonParser.parse(jsonString);
		return json;
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
		Connection con;
		try {
			con = getWebConnection();

			// JSONArray object = getTopics(con);
			// System.out.println("All topics");
			// System.out.println(Helper.niceJsonPrint(object, ""));
			//
			 JSONObject parameters;
			 JSONObject result;
			
			 parameters = new JSONObject();
			 parameters.put("topic", 1);
			 result = getFirstQuestionBox(con, parameters);
			 System.out
			 .println("First Question Box (delivered with topic details):");
			 System.out.println(result.toJSONString());
			 System.out.println();
			
			 parameters = new JSONObject();
			 parameters.put("questionbox", 1);
			 parameters.put("yes_count", 0);
			 result = getNextAction(con, parameters);
			 System.out.println("Questions answered with yes: 0");
			 System.out.println("Next Action: " + result.get("action"));
			 System.out.println(result.toJSONString());
			 System.out.println();
			//
			// parameters = new JSONObject();
			// parameters.put("questionbox", 1);
			// parameters.put("yes_count", 1);
			// result = getNextAction(con, parameters);
			// System.out.println("Questions answered with yes: 1");
			// System.out.println("Next Action: " + result.get("action"));
			// System.out.println(result.toJSONString());
			// System.out.println();
			//
			// parameters = new JSONObject();
			// parameters.put("questionbox", 1);
			// parameters.put("yes_count", 2);
			// result = getNextAction(con, parameters);
			// System.out.println("Questions answered with yes: 2");
			// System.out.println("Next Action: " + result.get("action"));
			// System.out.println(Helper.niceJsonPrint(result, ""));
			// System.out.println();

			// JSONObject ob = getTopCategories(con);
			// System.out.println(ob.toJSONString());
			// JSONObject in1 = new JSONObject();
			// in1.put("path", 5);
			// JSONObject res1 = getNextCategories(con, in1);
			// System.out.println(res1.toJSONString());
			//
			// JSONObject in2 = new JSONObject();
			// in2.put("path", "5.10");
			// JSONObject res2 = getNextCategories(con, in2);
			// System.out.println(res2.toJSONString());
			//
			// JSONObject in3 = new JSONObject();
			// in3.put("path", "5.10.13");
			// JSONObject res3 = getNextCategories(con, in3);
			// System.out.println(res3.toJSONString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
