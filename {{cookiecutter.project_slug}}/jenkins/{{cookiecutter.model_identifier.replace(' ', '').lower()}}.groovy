node {
    hasFailed = false
    sh 'sudo /var/lib/jenkins/jenkins-chown'
    deleteDir() // wipe out the workspace

    properties([
      parameters([
        [$class: 'StringParameterDefinition',  name: 'BUILD_BRANCH', defaultValue: 'master'],
        [$class: 'StringParameterDefinition',  name: 'MODEL_NAME', defaultValue: "{{cookiecutter.model_identifier.replace('\'' '\'', '\'''\'').upper()}}"],
        [$class: 'StringParameterDefinition',  name: 'MODEL_SUPPLIER', defaultValue: "{{cookiecutter.organization.replace(' ', '')}}"],
        [$class: 'StringParameterDefinition',  name: 'MODEL_BRANCH', defaultValue: BRANCH_NAME],
        [$class: 'StringParameterDefinition',  name: 'MODEL_DATA', defaultValue: ''],
        [$class: 'StringParameterDefinition',  name: 'MDK_BRANCH', defaultValue: 'develop'],
        [$class: 'StringParameterDefinition',  name: 'TAG_RELEASE', defaultValue: BRANCH_NAME.split('/').last() + "-${BUILD_NUMBER}"],
        [$class: 'StringParameterDefinition',  name: 'TAG_OASIS', defaultValue: ''],
        [$class: 'StringParameterDefinition',  name: 'RUN_TESTS', defaultValue: '0_case'],
        [$class: 'BooleanParameterDefinition', name: 'BUILD_WORKER', defaultValue: Boolean.valueOf(false)],
        [$class: 'BooleanParameterDefinition', name: 'PURGE', defaultValue: Boolean.valueOf(true)],
        [$class: 'BooleanParameterDefinition', name: 'PUBLISH', defaultValue: Boolean.valueOf(false)],
      ])
    ])

    // Build vars
    String build_repo      = 'git@github.com:OasisLMF/build.git'
    String build_branch    = params.BUILD_BRANCH
    String build_workspace = 'oasis_build'
    String PIPELINE        = script_dir + "/buildscript/pipeline.sh"
    String git_creds       = "<creds-key-here>"

    // Model vars
    String model_supplier   = params.MODEL_SUPPLIER
    String model_varient    = params.MODEL_NAME
    String model_branch     = params.MODEL_BRANCH
    String model_git_url    = "git@github.com:OasisLMF/{{cookiecutter.project_slug}}.git"
    String model_workspace  = "${model_varient}_workspace"
    String model_image      = "coreoasis/model_worker"
    String model_test_dir  = "${env.WORKSPACE}/${model_workspace}/tests/"
    String model_test_ini  = "autotest-config.ini"

    // Update MDK branch based on model branch
    if (model_branch.matches("master") || model_branch.matches("hotfix/(.*)")){
        MDK_BRANCH='master'
    } else {
        MDK_BRANCH=params.MDK_BRANCH
    }

    try {
        parallel(
            clone_build: {
                stage('Clone: ' + build_workspace) {
                    dir(build_workspace) {
                       git url: build_repo, credentialsId: git_creds, branch: build_branch
                    }
                }
            },
            clone_model: {
                stage('Clone Model') {
                    sshagent (credentials: [git_creds]) {
                        dir(model_workspace) {
                            sh "git clone --recursive ${model_git_url} ."
                            if (model_branch.matches("PR-[0-9]+")){
                                sh "git fetch origin pull/$CHANGE_ID/head:$BRANCH_NAME"
                                sh "git checkout $CHANGE_TARGET"
                                sh "git merge $BRANCH_NAME"

                            } else {
                                // Checkout branch
                                sh "git checkout ${model_branch}"
                            }
                        }
                    }
                }
            }
        )
        stage('Shell Env'){
            // Set Pipeline helper script
            func_script = env.WORKSPACE + "/" + build_workspace + '/buildscript/utils.sh'
            env.PIPELINE_LOAD = func_script

            // TESTING VARS
            env.TEST_MAX_RUNTIME = '190'
            env.TEST_DATA_DIR    = model_test_dir 
            env.MODEL_SUPPLIER   = model_supplier
            env.MODEL_VARIENT    = model_varient
            env.MODEL_ID         = '1'
            env.OASIS_MODEL_REPO_DIR = "${env.WORKSPACE}/${model_workspace}"
            env.OASIS_MODEL_DATA_DIR = params.MODEL_DATA
            env.COMPOSE_PROJECT_NAME = UUID.randomUUID().toString().replaceAll("-","")

            // Check if versions given, fallback to load from `oasis_version.json`
            def vers_data = readJSON file: "${env.WORKSPACE}/${model_workspace}/meta-data/oasis_version.json"
            env.IMAGE_WORKER     = model_image

            // RUN PLATFORM 
            if (params.TAG_OASIS?.trim()) {
                env.TAG_RUN_PLATFORM = params.TAG_OASIS 
                env.TAG_RUN_WORKER  = params.TAG_OASIS 
            } else {
                env.TAG_RUN_PLATFORM = vers_data['PLATFORM_VERSION']
                env.TAG_RUN_WORKER = vers_data['WORKER_VERSION']
            }

            // Print ENV
            sh  PIPELINE + ' print_model_vars'
        }

        // Build worker from oasis `develop` or Dockerfile (if applicable)
        if (params.BUILD_WORKER){
            env.TAG_RUN_WORKER   = params.TAG_RELEASE
            stage('Build Worker'){
                dir(build_workspace) {
                    sh  "docker build --no-cache -f docker/Dockerfile.worker-git --pull --build-arg worker_ver=${params.MDK_BRANCH} -t coreoasis/model_worker:${params.TAG_RELEASE} ."
                }
            }
        }

        stage('Run: API Server') {
            dir(build_workspace) {
                //sh PIPELINE + " start_model"
                sh "docker-compose -f compose/oasis.platform.yml -f compose/model.worker.data.yml up -d"
            }
        }

        api_server_tests = params.RUN_TESTS.split()
        for(int i=0; i < api_server_tests.size(); i++) {
            stage("Run : ${api_server_tests[i]}"){
                dir(build_workspace) {
                    compose = "docker-compose -f compose/oasis.platform.yml -f compose/model.worker.data.yml -f compose/model.tester.yml "
                    run_tester = "run --rm '--entrypoint=bash -c ' model_tester './runtest --config /var/oasis/test/${model_test_ini} --test-case ${api_server_tests[i]}'"
                    sh compose + run_tester
                }
            }
        }
    } catch(hudson.AbortException | org.jenkinsci.plugins.workflow.steps.FlowInterruptedException buildException) {
        hasFailed = true
        error('Build Failed')
    } finally {
        //Docker house cleaning
        dir(build_workspace) {
            sh 'docker-compose -f compose/oasis.platform.yml -f compose/model.worker.yml logs server-db      > ./stage/log/server-db.log '
            sh 'docker-compose -f compose/oasis.platform.yml -f compose/model.worker.yml logs server         > ./stage/log/server.log '
            sh 'docker-compose -f compose/oasis.platform.yml -f compose/model.worker.yml logs celery-db      > ./stage/log/celery-db.log '
            sh 'docker-compose -f compose/oasis.platform.yml -f compose/model.worker.yml logs rabbit         > ./stage/log/rabbit.log '
            sh 'docker-compose -f compose/oasis.platform.yml -f compose/model.worker.yml logs worker         > ./stage/log/worker.log '
            sh 'docker-compose -f compose/oasis.platform.yml -f compose/model.worker.yml logs worker-monitor > ./stage/log/worker-monitor.log '
            sh PIPELINE + " stop_docker ${env.COMPOSE_PROJECT_NAME}"
        }

        //Git tagging
        if(! hasFailed && params.PUBLISH){
            sshagent (credentials: [git_creds]) {
                dir(model_workspace) {
                    sh PIPELINE + " git_tag ${env.TAG_RELEASE}"
                }
            }
        }
        //Store logs
        dir(build_workspace) {
            archiveArtifacts artifacts: 'stage/log/**/*.*', excludes: '*stage/log/**/*.gitkeep'
            archiveArtifacts artifacts: "stage/output/**/*.*"
        }
    }
}
