package com.test;

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
import org.postgresql.ds.PGSimpleDataSource;

public class DataBaseFunctions {

	private static JSONParser jsonParser = new JSONParser();
	static String URL;
	static String DATABASE;
	static String PORT;
	static String USER;
	static String PASSWORD;

	private static PGSimpleDataSource pgSimpleDataSourceWeb;
	private static boolean loaded = false;

	public static Connection getWebConnection() {
		if (!loaded) {
			URL = "localhost";
			PORT = "5433";
			DATABASE = "learning2";
			USER = "postgres";
			PASSWORD = "postgres";
			loaded = true;
		}
		try {
			if (pgSimpleDataSourceWeb == null) {
				pgSimpleDataSourceWeb = new PGSimpleDataSource();
				pgSimpleDataSourceWeb.setServerName(URL);
				pgSimpleDataSourceWeb.setPortNumber(Integer.valueOf(PORT));
				pgSimpleDataSourceWeb.setDatabaseName(DATABASE);
				pgSimpleDataSourceWeb.setUser(USER);
				pgSimpleDataSourceWeb.setPassword(PASSWORD);

			}
			Connection con = pgSimpleDataSourceWeb.getConnection();
			con.setAutoCommit(true);
			return con;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
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
	 * 
	 */
	public static void insertNewQuestionPath(Connection con,
			JSONObject parameters) {
		JSONObject first = (JSONObject) parameters.clone();
		ArrayDeque<JSONObject> jobQueue = new ArrayDeque<JSONObject>();
		first.put("path", "");
		try {

			PreparedStatement topicStatement = con
					.prepareStatement("INSERT INTO topics (id,title,description) VALUES (default,?,?) RETURNING id");
			String topic_title = first.remove("topic_title").toString();
			String topic_description = first.remove("topic_description")
					.toString();
			topicStatement.setString(1, topic_title);
			topicStatement.setString(2, topic_description);
			ResultSet topicResult = topicStatement.executeQuery();
			if (!topicResult.next()) {
				// TODO
				System.out.println("Topic happenend");
				return;
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
					String questionString = "(" + question + "," + details
							+ ")";
					questionStrings[count++] = questionString;
					System.out
							.println("one question String: " + questionString);
				}
				Array questionArray = con.createArrayOf("question",
						questionStrings);
				System.out.println("Array: " + questionArray.toString());
				PreparedStatement pstmt = con
						.prepareStatement("INSERT into question_boxes (id,description,questions) VALUES (default,?,(?)::question[]) RETURNING id");

				pstmt.setString(1, description);
				pstmt.setArray(2, questionArray);

				ResultSet rs = pstmt.executeQuery();

				if (!rs.next())
					System.out.println("should not happen!");

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
					System.out.println(String.format(
							"Old path: %s\nNew path: %s", path, newPath));
					if (valueObject instanceof JSONObject) {
						System.out.println("Found inside box");
						JSONObject nextJob = (JSONObject) valueObject;
						nextJob.put("path", newPath);
						jobQueue.add(nextJob);
					} else if (valueObject instanceof String) {
						String treatment = valueObject.toString();
						System.out.println("Just found inside string: "
								+ treatment);
						PreparedStatement insertTreatmentStatement = con
								.prepareStatement("INSERT INTO treatments (id,description) VALUES (default,?) RETURNING id");
						insertTreatmentStatement.setString(1, treatment);
						ResultSet rs2 = insertTreatmentStatement.executeQuery();

						if (!rs2.next())
							throw new NullPointerException();
						int treatmentID = rs2.getInt(1);

						PreparedStatement pathInsertStatement = con
								.prepareStatement("INSERT INTO paths (id,path,treatment,topic) VALUES (default,(?)::ltree,?,?)");
						pathInsertStatement.setString(1, newPath);
						pathInsertStatement.setInt(2, treatmentID);
						pathInsertStatement.setInt(3, topic);
						System.out.println(pathInsertStatement.toString());
						pathInsertStatement.executeUpdate();
					} else
						System.out.println("Found strange thing inside");

				}

			}
			// con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			// TODO
		}

	}

	/**
	 * 
	 * @param con
	 * @param parameters
	 *            JSON Object with the following parameters:<br>
	 *            topic : (int)<br>
	 *            path : (String)
	 * @return One of the following JSON Objects:<br>
	 * {<br>
	 * result_type : "treatment",<br>
	 * treatment : (String)<br>
	 * }<br>
	 * <br>
	 * {<br>
	 * result_type : "question_box",<br>
	 * question_box:<br>
	 * 				{<br>
	 * 				description : (String),<br>
	 * 				questions : [<br>
	 * 							{<br>
	 * 							question : (String),<br>
	 * 							details : (String)<br>
	 * 							}<br>
	 * 							...<br>
	 * 							]<br>
	 * 				}<br>
	 * }
	 */
	public static JSONObject getNextQuestionBox(Connection con,
			JSONObject parameters) {
		String topicS = parameters.get("topic").toString();
		String path = parameters.get("path").toString();
		int topic = Integer.valueOf(topicS);
		JSONObject obj = null;
		try {
			PreparedStatement isEndStatement = con
					.prepareStatement("SELECT t.description FROM paths p,treatments t WHERE t.id = p.treatment AND p.topic = ? AND ('*.'||?)::lquery ~ p.path");

			isEndStatement.setInt(1, topic);
			isEndStatement.setString(2, path);

			JSONObject result = new JSONObject();
			ResultSet rs = isEndStatement.executeQuery();
			if (rs.next()) {
				String treatment = rs.getString(1);
				result.put("result_type", "treatment");
				result.put("treatment", treatment);
				return result;
			}

			PreparedStatement getBoxStatement = con
					.prepareStatement("WITH paras AS (SELECT 1 as topic, '3_0'::text as pa) SELECT row_to_json(row) FROM (SELECT description,array_to_json(questions) as questions FROM question_boxes q WHERE q.id IN (SELECT DISTINCT (regexp_split_to_array(ltree2text(subpath(p.path,nlevel(paras.pa::ltree)+index(p.path,paras.pa::ltree),1)),'_'))[1]::integer q_index FROM paths p,paras WHERE paras.topic = p.topic AND ('*.'||paras.pa||'.*')::lquery ~ p.path)) row");
			ResultSet resultBox = getBoxStatement.executeQuery();
			if (!resultBox.next()) {
				throw new SQLException();
			}

			String s = resultBox.getString(1);
			JSONObject jsonOb = (JSONObject) jsonParser.parse(s);

			result.put("result_type", "question_box");
			result.put("question_box", jsonOb);

			if (resultBox.next()) {
				System.out.println("There are more boxes (bad thing).");
			}

			return result;
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();
			return null;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
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
		Connection con = getWebConnection();
		// insertNewQuestionPath(con, questionBox1);
		JSONObject parameters = new JSONObject();
		parameters.put("topic", 1);
		parameters.put("path", "1_0");
		getNextQuestionBox(con, parameters);

	}

}
