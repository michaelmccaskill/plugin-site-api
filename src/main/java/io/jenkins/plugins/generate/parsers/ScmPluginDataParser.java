package io.jenkins.plugins.generate.parsers;

import io.jenkins.plugins.generate.PluginDataParser;
import io.jenkins.plugins.models.Plugin;
import io.jenkins.plugins.models.Scm;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class ScmPluginDataParser implements PluginDataParser {

  @Override
  public void parse(JSONObject pluginJson, Plugin plugin) {
    final String name = pluginJson.getString("name").endsWith("-plugin")
      ? pluginJson.getString("name")
      : pluginJson.getString("name") + "-plugin";
    final String scmString = StringUtils.trimToNull(pluginJson.optString("scm", null));
    final Scm scm = new Scm();
    final String issues = "http://issues.jenkins-ci.org/secure/IssueNavigator.jspa?mode=hide&reset=true" +
      "&jqlQuery=project+%3D+JENKINS+AND+status+in+%28Open%2C+%22In+Progress%22%2C+Reopened%29+AND+component+%3D+%27" +
      name + "%27";
    scm.setIssues(issues);
    if (StringUtils.endsWith(scmString, "github.com")) {
      final String link = "https://github.com/jenkinsci/" + name;
      final String baseCompareUrl = String.format("%s/compare/%s-", link, pluginJson.getString("name"));
      final String inLatestRelease = String.format("%s%s...%s-%s", baseCompareUrl,
        pluginJson.optString("previousVersion"), pluginJson.getString("name"), pluginJson.getString("version"));
      final String sinceLatestRelease = String.format("%s%s...master", baseCompareUrl, pluginJson.getString("version"));
      final String pullRequests = link + "/pulls";
      scm.setLink(link);
      scm.setInLatestRelease(inLatestRelease);
      scm.setSinceLatestRelease(sinceLatestRelease);
      scm.setPullRequests(pullRequests);
    }
    plugin.setScm(scm);
  }
}
