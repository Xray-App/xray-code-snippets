#!!/bin/env ruby

require 'rubygems'
require 'json'
require 'rest-client'
require 'pry'
require 'graphlient'
require 'optparse'



  class Parser
    def self.parse(args)
      options = {}
      opt_parser = OptionParser.new do |opts|
        opts.banner = "Usage: bundle exec ruby process_xray_cloud_backup.rb [options]"

        opts.on("-m", "--metadata METADATA_DIRECTORY", "Directory with extracted Xray Cloud backup metadata composed of multiple JSON files") do |dir|
          options[:metatada_dir] = dir
        end

        opts.on("-a", "--metadata ATTACHMENTS_DIRECTORY", "Directory with extracted Xray Cloud attachments backup composed of multiple attachment files") do |dir|
          options[:attachments_dir] = dir
        end

        opts.on("-i", "--id CLIENT_ID", "Xray Cloud client id from related API key") do |s|
          options[:client_id] = s
        end
      
        opts.on("-sCLIENT_SECRET", "--secret CLIENT_SECRET", "Xray Cloud client secret from related API key") do |s|
          options[:client_secret] = s
        end
    
        opts.on("-h", "--help", "Prints this help") do
          puts opts
          exit
        end
      end

      opt_parser.parse!(args)
      return options
    end
  end





  def get_xray_cloud_auth_token(client_id, client_secret)
    url = 'https://xray.cloud.getxray.app/api/v2/authenticate'
    request_data = {
      client_id: client_id,
      client_secret: client_secret
    }
    json_data = request_data.to_json
    response = RestClient.post(url, json_data, content_type: :json, accept: :json)
    response.body.gsub(/\A"|"\z/, '')
  end


  def obtain_testexec_keys_for_ids(client, ids)
    h = {}
    ids.each_slice(100) do |slice|
      variables = { issueIds: slice }
      response = client.query(TestExecsQuery, variables)
      response.to_h["data"]["getTestExecutions"]["results"].each do |result|
        h[result["issueId"]] = result["jira"]["key"]
      end
      
    end
    return h
  end

  def obtain_precondition_keys_for_ids(client, ids)
    h = {}
    ids.each_slice(100) do |slice|
      variables = { issueIds: slice }
      response = client.query(PreconditionsQuery, variables)
      response.to_h["data"]["getPreconditions"]["results"].each do |result|
        h[result["issueId"]] = result["jira"]["key"]
      end
      
    end
    return h
  end

  def obtain_test_keys_for_ids(client, ids)
    h = {}
    ids.each_slice(100) do |slice|
      variables = { issueIds: slice }
      response = client.query(TestsQuery, variables)
      response.to_h["data"]["getTests"]["results"].each do |result|
        h[result["issueId"]] = result["jira"]["key"]
      end
      
    end
    return h
  end

  def obtain_testset_keys_for_ids(client, ids)
    h = {}
    ids.each_slice(100) do |slice|
      variables = { issueIds: slice }
      response = client.query(TestSetsQuery, variables)
      response.to_h["data"]["getTestSets"]["results"].each do |result|
        h[result["issueId"]] = result["jira"]["key"]
      end
      
    end
    return h
  end

  def obtain_testplan_keys_for_ids(client, ids)
    h = {}
    ids.each_slice(100) do |slice|
      variables = { issueIds: slice }
      response = client.query(TestPlansQuery, variables)
      response.to_h["data"]["getTestPlans"]["results"].each do |result|
        h[result["issueId"]] = result["jira"]["key"]
      end
      
    end
    return h
  end


  def count_folders(hash)
    return 0 unless hash.is_a?(Hash) && hash.key?('folders')
  
    folder_count = hash['folders'].size
  
    # Recursively count subfolders
    hash['folders'].each do |subfolder|
      folder_count += count_folders(subfolder)
    end
  
    folder_count
  end


#############################################################

#Print the usage if there are no arguments
Parser.parse %w[--help] if ARGV.empty?
options = {}
begin
  options = Parser.parse ARGV
  raise OptionParser::MissingArgument if options[:metatada_dir].nil? || options[:attachments_dir].nil? || options[:client_id].nil? || options[:client_secret].nil?
rescue Exception => e
  puts "Exception encountered: #{e}"
  Parser.parse %w[--help]
  exit 1
end


TOKEN = get_xray_cloud_auth_token(options[:client_id], options[:client_secret])

TestPlansQuery  = <<-GRAPHQL
query($issueIds: [String]) {
  getTestPlans(issueIds: $issueIds, limit: 100) {  
    total
    start
    limit
    results {
        issueId
        jira(fields: ["key"])
    }
  }
}
GRAPHQL

TestSetsQuery  = <<-GRAPHQL
query($issueIds: [String]) {
  getTestSets(issueIds: $issueIds, limit: 100) {  
    total
    start
    limit
    results {
        issueId
        jira(fields: ["key"])
    }
  }
}
GRAPHQL

TestExecsQuery  = <<-GRAPHQL
query($issueIds: [String]) {
  getTestExecutions(issueIds: $issueIds, limit: 100) {  
    total
    start
    limit
    results {
        issueId
        jira(fields: ["key"])
    }
  }
}
GRAPHQL

TestsQuery  = <<-GRAPHQL
query($issueIds: [String]) {
  getTests(issueIds: $issueIds, limit: 100) {  
    total
    start
    limit
    results {
        issueId
        jira(fields: ["key"])
    }
  }
}
GRAPHQL

PreconditionsQuery  = <<-GRAPHQL
query($issueIds: [String]) {
  getPreconditions(issueIds: $issueIds, limit: 100) {  
    total
    start
    limit
    results {
        issueId
        jira(fields: ["key"])
    }
  }
}
GRAPHQL


graphql_client = Graphlient::Client.new('https://xray.cloud.getxray.app/api/v2/graphql',
  headers: {
    'Authorization' => "Bearer #{TOKEN}"
  },
  http_options: {
    read_timeout: 20,
    write_timeout: 30
  },
  allow_dynamic_queries: true
)


xraydata_dir = options[:metatada_dir]
if !File.directory?(xraydata_dir)
  print("Directory #{xraydata_dir} does not exist\n")
  exit
end
attachments_dir = options[:attachments_dir]
if !File.directory?(attachments_dir)
  print("Directory #{attachments_dir} does not exist\n")
  exit
end

test_repos = []
trepos_files = Dir.entries(xraydata_dir).grep(/^testRepository_/)
trepos_files.each do |trepo_file|
  test_repos.append( JSON.parse!(File.read(File.join(xraydata_dir, trepo_file))) )
end

test_environments = []
test_types = []
project_settings = []
psettings_files = Dir.entries(xraydata_dir).grep(/^(projectSettings_|settings.json)/)
psettings_files.each do |psettings_file|
  settingsObj = JSON.parse!(File.read(File.join(xraydata_dir, psettings_file)))
  settings = settingsObj["projectSettings"]
  settings["projectId"] = settingsObj["info"]["projectId"] if settingsObj["info"] && settingsObj["info"]["projectId"]
  test_environments.append( settings["testEnvironmentOptions"]["testEnvironments"] ) if settings["testEnvironmentOptions"]
  test_types.append( settings["testTypeOptions"]["testTypes"] ) if settings["testTypeOptions"]
  project_settings.append( settings ) if settingsObj["info"] && settingsObj["info"]["projectId"]
end
test_environments = test_environments.flatten().uniq
test_types = test_types.flatten().uniq

testplans = []
testplan_files = Dir.entries(xraydata_dir).grep(/^testPlans_/)
testplan_files.each do |testplan_file|
  testplans.append( JSON.parse!(File.read(File.join(xraydata_dir, testplan_file)))["testPlans"] )
end
testplans = testplans.flatten()

htestplans = {}
testplans.each do |testplan|
  #print(testplan["id"],"\n")
  htestplans[testplan["id"]] = testplan
end
testplan_ids = htestplans.keys.map { |k| k.to_s }
h = obtain_testplan_keys_for_ids(graphql_client, testplan_ids)
htestplans.each do |k, v|
  v["testPlanKey"] = h[k]
  v["testExecutions"] = []
end
#print(JSON.pretty_generate(htestplans))


preconditions = []
precondition_files = Dir.entries(xraydata_dir).grep(/^preconditions_/)
precondition_files.each do |precondition_file|
  preconditions.append( JSON.parse!(File.read(File.join(xraydata_dir, precondition_file)))["preconditions"] )
end
preconditions = preconditions.flatten()

hpreconditions = {}
preconditions.each do |precondition|
  hpreconditions[precondition["id"]] = precondition
end

precondition_ids = hpreconditions.keys.map { |k| k.to_s }
h = obtain_precondition_keys_for_ids(graphql_client, precondition_ids)
hpreconditions.each do |k, v|
  v["preconditionKey"] = h[k]
end
#print(JSON.pretty_generate(hpreconditions))


tests = []
test_files = Dir.entries(xraydata_dir).grep(/^tests_/)
test_files.each do |test_file|
    testsObj = JSON.parse!(File.read(File.join(xraydata_dir, test_file)))
    partialTests = testsObj["tests"]
    partialTests.each do |test|
      test["projectId"] = testsObj["info"]["projectId"]
    end
  	tests.append( partialTests )
end
tests = tests.flatten()

htests = {}
tests.each do |test|
  htests[test["id"]] = test
end

test_ids = htests.keys.map { |k| k.to_s }
h = obtain_test_keys_for_ids(graphql_client, test_ids)
htests.each do |k, v|
  v["testKey"] = h[k]
  # update the test repo having the same project id as the test, if present, adding a field named "projectKey"
  test_repos.each do |trepo|
    if trepo["info"]["projectId"] == v["projectId"]
      if v["testKey"]
        projectKey = v["testKey"].split("-")[0]
        trepo["info"]["projectKey"] = projectKey
        v["projectKey"] =  projectKey
      end
      break
    end
  end
end
#print(JSON.pretty_generate(htests))
#print(JSON.pretty_generate(test_repos))

testsets = []
testset_files = Dir.entries(xraydata_dir).grep(/^testSets_/)
testset_files.each do |testset_file|
  testsets.append( JSON.parse!(File.read(File.join(xraydata_dir, testset_file)))["testSets"] )
end
testsets = testsets.flatten()

htestsets = {}
testsets.each do |testset|
  htestsets[testset["id"]] = testset
end

testset_ids = htestsets.keys.map { |k| k.to_s }
h = obtain_testset_keys_for_ids(graphql_client, testset_ids)
htestsets.each do |k, v|
  v["testSetKey"] = h[k]
end
#print(JSON.pretty_generate(htestsets))

testexecs = []
testexec_files = Dir.entries(xraydata_dir).grep(/^testExecutions_/)
testexec_files.each do |testexec_file|
  	testexecs.append( JSON.parse!(File.read(File.join(xraydata_dir, testexec_file)))["testExecutions"] )
end
testexecs = testexecs.flatten()

htestexecs = {}
testexecs.each do |testexec|
  #print(testexec["id"],"\n")
  htestexecs[testexec["id"]] = testexec
  tps = testexec["testPlans"]
  if tps 
    tps.each do |tp_id|
      htestplans[tp_id]["testExecutions"].append(testexec)
    end
  end
end


testexec_ids = htestexecs.keys.map { |k| k.to_s }
h = obtain_testexec_keys_for_ids(graphql_client, testexec_ids)
htestexecs.each do |k, v|
  v["testExecKey"] = h[k]
end
#print(JSON.pretty_generate(htestexecs))


testruns = []
testrun_files = Dir.entries(xraydata_dir).grep(/^(archivedTestRuns_|testRuns_)/)
testrun_files.each do |testrun_file|
    tmp =  JSON.parse!(File.read(File.join(xraydata_dir, testrun_file)))
    if tmp["testRuns"]
  	  testruns.append( tmp["testRuns"] )
    elsif tmp["archivedTestRuns"]
      tmp["archivedTestRuns"].each do |tr|
        tr["archived"] = true
      end
      testruns.append( tmp["archivedTestRuns"] )
    end
end

testruns = testruns.flatten()

#print(JSON.pretty_generate(testruns))
htestruns = {}
testruns.each do |testrun|
  testrun["testEnvironments"] = htestexecs[testrun["testExecIssueId"]]["testEnvironments"]
  htestruns[testrun["_id"]] = testrun
end

attachs = []
meta_files = Dir.entries(attachments_dir).grep(/^metadata_/)
meta_files.each do |meta_file|
  arr = JSON.parse!(File.read(File.join(attachments_dir, meta_file)))["attachment_metadata"].map { |key, value| { key => value } }
	attachs.append(arr.flatten())
end

attachs = attachs.flatten()
#print(JSON.pretty_generate(attachs))


idx = 0
attachs.each do |attach|
 id = attach.keys.first
 attach[id]["size"] = File.size( File.join(attachments_dir, id) )

  if attach[id]["entity"] == "TEST" && attach[id]["id"]
    attach[id]["testKey"] = htests[attach[id]["id"]]["testKey"]
  end
  if attach[id]["entity"] == "TESTRUN" && attach[id]["id"]
    tr = htestruns[attach[id]["id"]]
    if tr
      attach[id]["testKey"] = htests[tr["testIssueId"]]["testKey"] 
      attach[id]["testExecKey"] = htestexecs[tr["testExecIssueId"]]["testExecKey"]
      attach[id]["archived"] = tr["archived"]
    else
      binding.pry
    end
  end

  if attach[id]["testid"]
    attach[id]["testKey"] = htests[attach[id]["testid"]]["testKey"]
  end
  if attach[id]["testexecid"]
    attach[id]["testExecKey"] = htestexecs[attach[id]["testexecid"]]["testExecKey"]
  end

 attachs[idx] = attach
 idx+=1 
end
#print(JSON.pretty_generate(attachs))

sorted = attachs.sort_by { |hash| -hash[hash.keys.first]['size'] }

print("========================================================================\n")
print("========== Top test runs by attachment size ============================\n")
# print the top test runs having the biggest attachments
# for the first 10 elements, print attachment entity, test key, test execution key, size in MB, archived in a tabular format, delimited by tabs
print("\nentity\t\ttestKey\t\ttestExecKey\t\tsize (MB)\t\tarchived\n")
sorted[0..9].each do |attach|
  id = attach.keys.first
  print("#{attach[id]["entity"]}\t\t#{attach[id]["testKey"]}\t\t#{attach[id]["testExecKey"]}\t\t#{attach[id]["size"]/1024/1024}\t\t#{attach[id]["archived"]}\n")
end
print("\n")
print("========================================================================\n")


# calculate the sum of sizes of all attachments based on project
sums = {}
sorted.each do |attach|
 id = attach.keys.first
 project = nil
 if attach[id]["testExecKey"]
    project = attach[id]["testExecKey"].split("-")[0]
 elsif attach[id]["testKey"]
  project = attach[id]["testKey"].split("-")[0]
 else
    print("\nNo test or test execution found for attachment #{attach}\n")
 end

 if project
    if sums[project]
      sums[project] += attach[id]["size"]
    else
      sums[project] = attach[id]["size"]
    end

    # if attach is linked to an archived TestRun, add it to a different project having the same name but with "archived" suffix
    if attach[id]["archived"]
      archived_project = "#{project}-archived"
      if sums[archived_project]
        sums[archived_project] += attach[id]["size"]
      else
        sums[archived_project] = attach[id]["size"]
      end
    end


  end
end
#print(JSON.pretty_generate(sums))

# sort the sums hash by size, descending, and return an array
sorted_sums = sums.sort_by { |k, v| -v }.to_h
#print(JSON.pretty_generate(sorted_sums),"\n")

print("========================================================================\n")
print("\nProject\t\tSize (MB)\n")
# print each project size in MB, formatted as a table
sorted_sums.each do |k, v|
  print("#{k}\t\t#{v/1024/1024}\n")
end
# print total of sums
print("========================================================================\n")
print("Total\t\t#{sorted_sums.values.sum/1024/1024} MB\n")

print("========================================================================\n")
print("========================================================================\n")



print("Total tests: #{tests.length}\n")
# print unique test types by their value
print("Test types: #{test_types.map { |t| t["value"] }.uniq}\n")
# print total tests grouped by type
tests.group_by { |t| t["type"] }.each do |k, v|
  # print the test type name based on the value on the test_types array
  print("#{test_types.select { |tt| tt["_id"] == k }.first["value"]}\t\t#{v.length}\n")
end

# print the 3 test repos having the most folders, inside the testRepository attribute, including the project id and the number of folders; the folders can contain other folders and we want to consider the total number of folders, including subfolders
print("\n")
print("3 test repositories, by project ID and project key, if present, having the most folders in Jira:\n")
print("Project ID\t\tProject Key\t\tTotal folders\n")
test_repos.sort_by { |tp| -count_folders(tp["testRepository"]) }[0..2].each do |tp|
  print("#{tp["info"]["projectId"]}\t\t\t#{tp["info"]["projectKey"]}\t\t\t#{count_folders(tp["testRepository"])}\n")
end
print("\n")


# print the total tests having no steps
print("\nTotal tests having no steps: #{tests.select { |t| t["steps"] && t["steps"].empty? }.length}\n")
print("Total preconditions: #{preconditions.length}\n")
# print the total of tests having no preconditions
print("Total tests having no preconditions: #{tests.select { |t| t["preConditionTargetIssueIds"].nil? || t["preConditionTargetIssueIds"].empty? }.length}\n")
print("Total preconditions being referenced by tests: #{tests.select { |t| t["preConditionTargetIssueIds"] && !t["preConditionTargetIssueIds"].empty? }.length}\n")
# print total of orphan preconditions, having in mind that tests using preconditions use a field called preConditionTargetIssueIds
tmph = hpreconditions
tests.select { |t| t["preConditionTargetIssueIds"] && !t["preConditionTargetIssueIds"].empty? }.each do |t|
  t["preConditionTargetIssueIds"].each do |precondition_id|
    tmph.delete(precondition_id)
  end
end
print("Total orphan preconditions: #{tmph.length}\n")


print("Total test sets: #{testsets.length}\n")
# print the total number of test sets having no tests
print("Total test sets having no tests: #{testsets.select { |ts| ts["tests"] && ts["tests"].empty? }.length}\n")
# print the 3 test sets having the most tests, including the key and the number of tests
print("3 test sets having the most tests:\n")
testsets.sort_by { |ts| -ts["tests"].length }[0..2].each do |ts|
  print("#{ts["testSetKey"]}\t\t#{ts["tests"].length}\n")
end


# print the total number of test plans
print("\nTotal test plans: #{testplans.length}\n")
# print the total number of test plans having no tests
print("Total test plans having no tests: #{testplans.select { |tp| tp["tests"] && tp["tests"].empty? }.length}\n")
# print the 3 test plans having the most tests, including the key and the number of tests
print("3 test plans having the most tests:\n")
testplans.sort_by { |tp| -tp["tests"].length }[0..2].each do |tp|
  print("#{tp["testPlanKey"]}\t\t#{tp["tests"].length}\n")
end
# print the 3 test plans having the most folders, inside the testPlanBoard attribute, if present, including the key and the number of folders; the folders can contain other folders and we want to consider the total number of folders, including subfolders
print("\n")
print("3 test plans having the most folders in the Board:\n")
testplans.select { |tp| tp["testPlanBoard"] }.sort_by { |tp| -count_folders(tp["testPlanBoard"]) }[0..2].each do |tp|
  print("#{tp["testPlanKey"]}\t\t#{count_folders(tp["testPlanBoard"])}\n")
end
print("3 test plans having the most Test Executions:\n")
testplans.sort_by { |tp| -tp["testExecutions"].length }[0..2].each do |tp|
  print("#{tp["testPlanKey"]}\t\t#{tp["testExecutions"].length}\n")
end
print("Total test executions: #{testexecs.length}\n")
print("Total test runs: #{testruns.length}\n")
print("Test environments: #{test_environments}\n")

# for each existent test environment, print the total of test runs, sorted by total, descending
print("\nTest runs per Test Environment:\n")
print("Test Environment\t\tTotal\n")
test_environments.sort_by { |te| -testruns.select { |tr| tr["testEnvironments"].include?(te) }.length }.each do |te|
  print("#{te}\t\t#{testruns.select { |tr| tr["testEnvironments"].include?(te) }.length}\n")
end
# print the total of test runs without test environments
print("Test runs without Test Environment\t\t#{testruns.select { |tr| tr["testEnvironments"].empty? }.length}\n")

# obtain all the issue keys from preconditions, tests, testexecs, testplans, and print the total of different projects using them
precondition_keys = preconditions.map { |precondition| precondition["preconditionKey"] }
test_keys = tests.map { |test| test["testKey"] }
testset_keys = testsets.map { |ts| ts["testSetKey"] }
testplan_keys = testplans.map { |tp| tp["testPlanKey"] }
testexec_keys = testexecs.map { |te| te["testExecKey"] }
all_keys = precondition_keys + test_keys + testset_keys + testplan_keys + testexec_keys
# iterate over all keys, split them by "-" and get the first element, which is the project name, and count the total of different projects
all_xray_related_projects = all_keys.flatten.map { |k| k.split("-")[0] if !k.nil? }.uniq.reject {|data| data.nil?}
print("Total different projects using Preconditions, Tests, Test Sets, Test Executions, or Test Plans: #{all_xray_related_projects.length}\n")


# print total of projects
print("\nTotal projects: #{project_settings.length}\n")
# print total of project in test repos
print("Total projects having a test repository: #{test_repos.length}\n")

# based on project settings, print total projects having issue types on the issueTypeIds attribute, to inform about coverage
print("Total projects with coverage enabled (i.e., having coverable issue types configured): #{project_settings.select { |st| st["testCoverage"]["issueTypeIds"] && !st["testCoverage"]["issueTypeIds"].empty? if st["testCoverage"]}.length}\n")
# based on project settings, print total projects having issue types on the defectMapping attribute
print("Total projects with defects mapping (i.e., having defect issue types configured): #{project_settings.select { |st| st["defectMapping"] && !st["defectMapping"].empty? }.length}\n")

# print the top 3 tests with most failures, including the test key and the total of failures, based on the test runs
print("\nTop 3 tests with most failures:\n")
print("Test Key\t\tTotal failures\n")
testruns.select { |tr| tr["status"] == "FAILED" }.group_by { |tr| tr["testIssueId"] }.sort_by { |k, v| -v.length }[0..2].each do |k, v|
  print("#{htests[k]["testKey"]}\t\t\t#{v.length}\n")
end

exit