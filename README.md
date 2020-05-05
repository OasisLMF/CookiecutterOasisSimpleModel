<img src="https://oasislmf.org/packages/oasis_theme_package/themes/oasis_theme/assets/src/oasis-lmf-colour.png" alt="Oasis LMF logo" width="250"/>

CookiecutterOasisSimpleModel
============================

A cookiecutter project template for creating local projects and Git repositories for standard (non-complex) Oasis models using the <a href="https://pypi.python.org/pypi/cookiecutter" target="_blank">`cookiecutter`</a> Python tool.

## First steps

Install cookiecutter (if not present):

    /home/foo$ pip install cookiecutter
    
Run cookiecutter on the source repo (the URI can be specified using either `https` or `git` or `git+ssh`):

    /home/foo$ cookiecutter git+{https,ssh}://git@github.com/OasisLMF/CookiecutterOasisSimpleModel

You should see the following prompts for project and model settings in sequence (press ENTER to use default values):
    
    project_name [OM]: 
    project_slug [OM]: 
    project_short_description [Oasis Simple Model]: 
    project_maintainer [<full name of primary project maintainer>]:
    project_maintainer_email [<primary GitHub account email of primary project maintainer>]: 
    version [0.0.1]: 
    primary_language [Python]: 
    organization [OasisLMF]: 
    model_identifier [OM]: 
    model_version [0.0.0.1]: 
    model_lookup_type [simple]: 

These prompts are self-explanatory, but `project_name`, `project_slug`, `organization`, `model_identifier` and `model_version` are mandatory, while the others are optional. Here are some guidelines to follow for the mandatory prompts.

* `project_name` - this should be a concise title for the project (title words should be capitalised)
* `project_slug` - this is the folder name for the project and by default cookiecutter will set this to a camel casing of the `project_name` value, but you may enter a specific value yourself, provided it does not contain spaces or any special characters not normally present in folder names
* `organization` - this should either be a camel case of the organization name or an acronym
* `model_identifier` - this should be a short ID for the full name of the initial model, e.g. an acronym of the full model name
* `model_version` - this can be any meaningful string that indicates a version for the initial model (by default it is set to `0.0.0.1`)
* `model_lookup_type` - "custom" for a custom lookup, or "builtin" for the builtin lookup provided by the MDK 

The project structure is contained in the repo folder named <a href="https://github.com/OasisLMF/CookiecutterOasisSimpleModel/tree/master/%7B%7Bcookiecutter.project_slug%7D%7D" target="_blank">`{{cookiecutter.project_slug}}`</a> and project-related settings such as the project descriptive name, model name and version etc., which are set during runtime via the prompts, are configurable in the repo file <a href="https://github.com/OasisLMF/CookiecutterOasisSimpleModel/blob/master/cookiecutter.json" target="_blank">`cookiecutter.json`</a>.

For the current state of the <a href="https://github.com/OasisLMF/cookiecutter-OasisModel/tree/master/%7B%7Bcookiecutter.project_slug%7D%7D" target="_blank">`{{cookiecutter.project_slug}}`</a> directory you should see the following project structure in the place where you ran the command (assuming you used default boilerplate values for the project name, organization and model name):

```
OasisModel/
├── jenkins
│   └── om.groovy
├── keys_data
│   └── OM
│       └── ModelVersion.csv
├── LICENSE
├── meta-data
│   ├── model_settings.json
│   └── oasis_version.json
├── model_data
│   └── OM
│       ├── damage_bin_dict.bin
│       ├── damage_bin_dict.csv
│       ├── data.csv
│       ├── events.bin
│       ├── events.csv
│       ├── footprint.bin
│       ├── footprint.csv
│       ├── footprint.idx
│       ├── ModelVersion.csv
│       ├── occurrence.bin
│       ├── occurrence.csv
│       ├── random.bin
│       ├── random.csv
│       ├── returnperiods.bin
│       ├── returnperiods.csv
│       ├── vulnerability.bin
│       └── vulnerability.csv
├── oasislmf.json
├── README.md
├── src
│   └── keys_server
│       └── OM
│           ├── __init__.py
│           └── OMKeysLookup.py
└── tests
    ├── autotest-config.ini
    ├── inputs
    │   ├── analysis_settings.json
    │   └── oed_example
    │       ├── account.csv
    │       ├── location.csv
    │       ├── ri_info.csv
    │       └── ri_scope.csv
    └── testing_readme.md
```

## Documentation
* <a href="https://oasislmf.github.io">General Oasis documentation</a>
* <a href="https://oasislmf.github.io/docs/oasis_mdk.html">Oasis MDK documentation</a>

## License
The code in this project is licensed under BSD 3-clause license.
