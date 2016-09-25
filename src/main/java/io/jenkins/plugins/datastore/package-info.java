/**
 * <p>Data tier for the application</p>
 *
 * <p>The data is currently housed in an embedded Elasticsearch node that is only accessible within the same JVM as the
 * application. The jenkins-infra team specifically did not want any external dependencies or resources if it could be
 * helped. This implementation will need to be re-evaluated in the near future as Elastic has announced the end of
 * embedded servers</p>
 */
package io.jenkins.plugins.datastore;
