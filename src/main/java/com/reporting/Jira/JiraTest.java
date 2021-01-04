/*
MIT License

Copyright (c) 2021 Dipjyoti Metia

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.reporting.Jira;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.reporting.Jira.JiraConfig.JIRA_REST_PATH;

public class JiraTest {

    private static final Logger logger = LogManager.getLogger();

    private static final String ISSUE_PATH = "issue/";

    private JiraTest() {
    }

    /**
     * Create and send a PUT request to JIRA to change the value of a field.
     */
    public static void changeIssueFieldValue(
            String issueKey, String fieldToUpdate, String resultValue) {

        JSONObject obj = new JSONObject();
        JSONObject fieldObj = new JSONObject();
        JSONArray setArr = new JSONArray();
        JSONObject setObj = new JSONObject();
        JSONObject valueObj = new JSONObject();

        try {
            obj.put("update", fieldObj);
            fieldObj.put(getFieldId(fieldToUpdate), setArr);
            setArr.put(setObj);
            setObj.put("set", valueObj);
            valueObj.put("value", resultValue);

            JiraConfig.getJIRARequestSpec()
                    .contentType("application/json").and()
                    .body(obj.toString())
                    .when()
                    .put(JIRA_REST_PATH + ISSUE_PATH + issueKey);
        } catch (JSONException e) {
            logger.error("Can't create JSON Object for test case result update", e);
        }
    }

    private static String getFieldId(String fieldName) {

        return JiraConfig.getJIRARequestSpec()
                .when()
                .get(JIRA_REST_PATH + "/field")
                .thenReturn().jsonPath()
                .getString(String.format("find {it.name == '%s'}.id", fieldName));
    }

    /**
     * Create and post a JSON request for a comment update in JIRA.
     */
    public static void addComment(String issueKey, String commentToAdd) {

        JSONObject obj = new JSONObject();

        try {
            obj.put("body", commentToAdd);
            JiraConfig.getJIRARequestSpec()
                    .contentType("application/json")
                    .body(obj.toString())
                    .when()
                    .post(JIRA_REST_PATH + ISSUE_PATH + issueKey + "/comment");
        } catch (JSONException e) {
            logger.error("Can't create JSON Object for comment update", e);
        }
    }

    /**
     * Create and post a JSON request for a transition change in JIRA.
     */
    public static void transitionIssue(String issueKey, String transitionName) {

        logger.debug("Transition name: " + transitionName);
        transitionIssue(issueKey, getTransitionId(issueKey, transitionName));
    }

    private static void transitionIssue(String issueKey, int transitionId) {

        logger.debug("Transition id: " + transitionId);
        JSONObject obj = new JSONObject();
        JSONObject idObj = new JSONObject();

        try {
            obj.put("transition", idObj);
            idObj.put("id", transitionId);
            JiraConfig.getJIRARequestSpec()
                    .contentType("application/json").and()
                    .body(obj.toString())
                    .when()
                    .post(JIRA_REST_PATH + ISSUE_PATH + issueKey + "/transitions");
        } catch (JSONException e) {
            logger.error("Can't create JSON Object for transition change", e);
        }
    }

    private static int getTransitionId(String issueKey, String transitionName) {

        return JiraConfig.getJIRARequestSpec()
                .get(JIRA_REST_PATH + ISSUE_PATH + issueKey + "?expand=transitions.fields")
                .thenReturn().jsonPath()
                .getInt(String.format(
                        "transitions.find {it -> it.name == '%s'}.id", transitionName));
    }

}