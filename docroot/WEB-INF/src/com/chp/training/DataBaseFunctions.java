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
import org.hsqldb.types.Types;
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
		if (connection == null || connection.isClosed()) {
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


	public static JSONObject addCategory(Connection con, JSONObject parameters) {
		String category = parameters.get("category_name").toString();
		Integer parent = Integer.valueOf(parameters.get("parent_id").toString());
		String material_title = parameters.get("material_title").toString();
		String material_text = parameters.get("material_text").toString();
		String material_pic = parameters.get("material_pic").toString();
		Boolean insert_pic = Boolean.valueOf(material_pic);
		
		PreparedStatement addCat = null;
		try {
			addCat = con.prepareStatement("INSERT INTO " +
					"categories (id,title,parent_category,material_title,material_text,material_pic) " +
					"VALUES (default,?,?,?,?,CASE WHEN ? THEN (SELECT MAX(material_pic)+1 FROM categories) ELSE NULL END) RETURNING id,material_pic");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			int p = 0;
			addCat.setString(p++, category);
			addCat.setInt(p++, parent);
			addCat.setString(p++, material_title);
			addCat.setString(p++, material_text);
			addCat.setBoolean(p++, insert_pic);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ResultSet rs;
		
		try {
			JSONObject result = new JSONObject();
			rs = addCat.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				int pic = rs.getInt(2);
				result.put("id", id);
				result.put("pic", pic);
				result.put("category:", category);
			}
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}

	public static JSONObject addMaterial(Connection con, JSONObject parameters) {
		String categoryS = parameters.get("category_id").toString();
		String material_title = parameters.get("material_title").toString();
		String material_text = parameters.get("material_text").toString();
		String material_pic = parameters.get("material_pic").toString();
		Boolean insert_pic = Boolean.valueOf(material_pic);
		
		PreparedStatement addCat = null;
		try {
			addCat = con.prepareStatement("UPDATE categories SET material_title = ?, " +
					"material_text = ?, material_pic = CASE WHEN ? THEN (SELECT MAX(material_pic)+1 FROM categories) ELSE material_pic END WHERE id = ? RETURNING id,material_pic");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			int p = 1;
			addCat.setString(p++, material_title);
			addCat.setString(p++, material_text);
			addCat.setBoolean(p++, insert_pic);
			addCat.setInt(p++, Integer.valueOf(categoryS));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ResultSet rs;
		
		try {
			JSONObject result = new JSONObject();
			rs = addCat.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				int pic = rs.getInt(1);
				result.put(id, pic);
			}
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}

	public static JSONObject getCategories(Connection con, JSONObject parameters) {
		Object parentO = parameters.get("category");
		String parent = parentO == null?"NaN":parentO.toString();
		PreparedStatement pstmt = null;
		try {
			pstmt = con
					.prepareStatement("SELECT id,title FROM categories c WHERE parent_category IS NOT DISTINCT FROM ?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			if (parent.equals("NaN")) {
				pstmt.setNull(1, Types.INTEGER);
			} else {
				pstmt.setInt(1, Integer.valueOf(parent));
			}
		} catch (SQLException e) {

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

	public static JSONObject getMaterial(Connection con, JSONObject parameters) {
		Integer parent = Integer.valueOf(parameters.get("category_id").toString());
		PreparedStatement pstmt = null;
		try {
			pstmt = con
					.prepareStatement("SELECT material_title,material_text,material_pic FROM categories c WHERE id = ?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			pstmt.setInt(1, Integer.valueOf(parent));
		} catch (SQLException e) {

		}

		ResultSet rs;
		JSONObject resultObject = new JSONObject();
		try {
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String material_title = rs.getString(1);
				String material_text = rs.getString(2);
				int material_pic = rs.getInt(3);
				resultObject.put("material_title", material_title);
				resultObject.put("material_text", material_text);
				resultObject.put("material_pic", material_pic);
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

	private static int insertQuestionBox(Connection con, JSONObject questionBox)
			throws SQLException {

		JSONArray jsonQuestions = (JSONArray) questionBox.remove("questions");
		// System.out.println("Questions: " + jsonQuestions.toString());
		Object descriptionO = questionBox.remove("description");

		String description = descriptionO == null ? "" : descriptionO
				.toString();
		int questionNum = jsonQuestions.size();
		// String[] questionStrings = new String[questionNum];
		int count = 0;
		String[] q_questionss = new String[questionNum];
		String[] q_details = new String[questionNum];

		for (Object questionObject : jsonQuestions) {
			JSONObject jsonQuestion = (JSONObject) questionObject;
			String question = jsonQuestion.get("question").toString();
			Object questionDetO = jsonQuestion.get("details");
			String questionDet = questionDetO == null ? "" : questionDetO
					.toString();
			q_questionss[count] = question;
			q_details[count++] = questionDet;
		}

		try {

			Array questionArray = con.createArrayOf("text", q_questionss); // ((?,?)::question)
			Array detailArray = con.createArrayOf("text", q_details); // ((?,?)::question)

			insertQuestionBoxStatement.setString(1, description);
			insertQuestionBoxStatement.setArray(2, questionArray);
			insertQuestionBoxStatement.setArray(3, detailArray);

		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Adding parameters to the statement failed\n"
							+ "Statement: %s\n"
							+ "Function: insertNewQuestionPath()\n"
							+ "QuestionBox JSON: %s\n" + "Details: %s",
					insertQuestionBoxStatement.toString(),
					Helper.niceJsonPrint(questionBox, ""), e.getMessage()));
		}
		ResultSet rs;
		try {
			rs = insertQuestionBoxStatement.executeQuery();
			if (!rs.next())
				throw new SQLException(String.format(
						"Statement did not return expected Result.\n"
								+ "Function: insertNewQuestionPath()\n"
								+ "Statement: %s\n" + "QuestionBox JSON: %s\n",
						insertTopicStatement.toString(),
						Helper.niceJsonPrint(questionBox, "")));
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Execution of Statement failed.\n"
							+ "Function: insertNewQuestionPath()\n"
							+ "Statement: %s\n" + "QuestionBox JSON: %s\n"
							+ "Details: %s",
					insertQuestionBoxStatement.toString(),
					Helper.niceJsonPrint(questionBox, ""), e.getMessage()));
		}

		int questionBoxID = rs.getInt(1);

		return questionBoxID;
	}

	/**
	 * 
	 * @param con
	 * @param parameters Check the developer's guide.
	 * @throws SQLException
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static void insertNewQuestionPath(Connection con,
			JSONObject parameters) throws SQLException {
		boolean firstB = true;
		JSONObject first = (JSONObject) parameters.clone();
		ArrayDeque<JSONObject> jobQueue = new ArrayDeque<JSONObject>();

		// Put first QuestionBox job Queue

		jobQueue.add(first);

		int topic = -1;

		while (!jobQueue.isEmpty()) {
			JSONObject questionBoxJob = jobQueue.pop();
			System.out.println("This is a full job: "
					+ questionBoxJob.toJSONString());
			Set<Object> testkeys = questionBoxJob.keySet();
			System.out.println("Keys of job:");
			for (Object key : testkeys)
				System.out.println(key.toString() + " : "
						+ questionBoxJob.get(key).toString());
			System.out.println();
			JSONObject questionBoxObject = (JSONObject) questionBoxJob
					.remove("questionbox");
			// System.out.println("Found Box");

			int questionBoxID = insertQuestionBox(con, questionBoxObject);

			// System.out.println("ID of Box: " + questionBoxID);

			if (!firstB) {
				JSONObject source = (JSONObject) questionBoxJob.get("source");
				String source_yes_list = source.get("source_yes_list")
						.toString();
				String[] source_yes_Strings = source_yes_list.split(",");
				Integer source_id = Integer.valueOf(source.get("source_id")
						.toString());
				// System.out.println("these sources for box " + questionBoxID +
				// ":");
				for (String source_yes_String : source_yes_Strings) {
					// System.out.println(source_id);
					insertActionStatement.setInt(1, source_id);
					insertActionStatement.setInt(2,
							Integer.valueOf(source_yes_String));
					insertActionStatement.setString(3, "next_box");
					insertActionStatement.setInt(4, questionBoxID);
					insertActionStatement.addBatch();
				}
				// System.out.println("-");
				insertActionStatement.executeBatch();
			} else {

				// Prepare Statement to insert topic

				firstB = false;
				try {

					JSONObject topicObject = (JSONObject) questionBoxJob
							.remove("topic");
					String topic_title = topicObject.get("title").toString();
					Object topic_descriptionOb = topicObject
							.remove("description");
					String topic_description = topic_descriptionOb == null ? ""
							: topic_descriptionOb.toString();
					insertTopicStatement.setString(1, topic_title);
					insertTopicStatement.setString(2, topic_description);
					insertTopicStatement.setInt(3, questionBoxID);
				} catch (SQLException e) {
					throw new SQLException(String.format(
							"Adding parameters to the statement failed\n"
									+ "Function: insertNewQuestionPath()\n"
									+ "Statement: %s\n" + "Parameters: %s\n"
									+ "Details: %s",
							insertTopicStatement.toString(),
							Helper.niceJsonPrint(parameters, ""),
							e.getMessage()));
				}
				ResultSet topicResult;

				// Execute Statement to prepare topic

				try {
					topicResult = insertTopicStatement.executeQuery();
				} catch (SQLException e) {
					throw new SQLException(String.format(
							"Execution of Statement failed.\n"
									+ "Function: insertNewQuestionPath()\n"
									+ "Statement: %s\n" + "Parameters: %s\n"
									+ "Details: %s",
							insertTopicStatement.toString(),
							Helper.niceJsonPrint(parameters, ""),
							e.getMessage()));
				}
				if (!topicResult.next()) {
					throw new SQLException(String.format(
							"Statement did not return expected Result.\n"
									+ "Function: insertNewQuestionPath()\n"
									+ "Statement: %s\n" + "Parameters: %s\n",
							insertTopicStatement.toString(),
							Helper.niceJsonPrint(parameters, "")));
				}
				topic = topicResult.getInt(1);
			}

			// System.out.println("ID of box: " + questionBoxID);
			// System.out.println(questionBoxJob.toJSONString());

			Set<Object> keySet = questionBoxJob.keySet();

			for (Object keyObject : keySet) {

				String keyString = keyObject.toString();
				// System.out.println("Next key: " + keyString);
				// System.out.println("there is a test of \"" + keyString +
				// "\"");
				if (!keyString.matches("(,*[0-9]*,*)*"))
					continue;
				// System.out.println("got here");

				JSONObject valueObject = (JSONObject) questionBoxJob
						.get(keyObject);

				String action = valueObject.get("action").toString();

				String[] keys = keyString.split(",");

				if ("next_box".equals(action)) {
					JSONObject source_obj = new JSONObject();
					source_obj.put("source_id", questionBoxID);
					source_obj.put("source_yes_list", keyString);

					JSONObject next_job = (JSONObject) valueObject;
					next_job.put("source", source_obj);
					jobQueue.add(next_job);
					continue;
				}

				for (String key : keys) {
					System.out.println("Trying to insert action: " + action);
					if ("change_topic".equals(action)) {

					} else if ("next_topic".equals(action)) {
						insertActionStatement.setInt(1, questionBoxID);
						insertActionStatement.setInt(2, Integer.valueOf(key));
						insertActionStatement.setString(3, "next_topic");
						insertActionStatement.setInt(4, topic);
						insertActionStatement.execute();
					} else if ("treatment".equals(action)) {
						// System.out.println("found the treatment");
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
	 * @param parameters Check developer's guide
	 * @throws SQLException
	 * @throws ParseException
	 */
	public static JSONObject getNextAction(Connection con, JSONObject parameters)
			throws SQLException, ParseException {
		String questionboxS = parameters.get("questionbox").toString();
		Integer questionBox = Integer.valueOf(questionboxS);
		String yesCountS = parameters.get("yes_count").toString();
		Integer yesCount = Integer.valueOf(yesCountS);

		System.out.println(yesCount
				+ " questions haven been answered with yes.");

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
		System.out.println(pstmt.toString());
		ResultSet rs;
		try {
			rs = pstmt.executeQuery();
		} catch (SQLException e) {
			String error = String.format("Execution of Statement failed.\n"
					+ "Function: getNextQuestionBox()\n" + "Statement: %s\n"
					+ "Parameters: %s\n" + "Details: %s", pstmt.toString(),
					Helper.niceJsonPrint(parameters, ""), e.getMessage());
			// System.out.println("Error message: " +error);
			throw new SQLException(error);
		}
		if (!rs.next()) {
			return null;
		}
		String jsonString = rs.getString(1);
		System.out.println("This is the String: " + jsonString);
		JSONObject json = (JSONObject) jsonParser.parse(jsonString);
		// System.out.println("This is the json: ");
		// System.out.println(Helper.niceJsonPrint(json, ""));
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

}
