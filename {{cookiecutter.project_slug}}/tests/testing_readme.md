Notes on Automated testing
==========================
This repository can be hooked into our [Jenkins CI server](https://ci.oasislmfdev.org/) which is able to pubish and tests models.

* Create sets of example OED files
* Add expected loss outputs under `tests/outputs/<test_case_name>`
* Update the file `tests/autotest-config.ini` with the (OED inputs / expected outputs).
* Setup the Jenkins job using `jenkins/{{cookiecutter.model_identifier.replace('\'' '\'', '\'''\'').lower()}}.groovy`

## Example OED files (FM24)
There is a  example of example [OED](https://github.com/Simplitium/OED) files under `tests/inputs/example` these are from a validation test for the Oasis Financial module `fmcalc`. [More examples](https://github.com/OasisLMF/OasisLMF/tree/master/validation/examples/)
It should be replaced with a reference set of OED files for testing the {{cookiecutter.model_identifier.replace('\'' '\'', '\'''\'').upper()}} model. 
