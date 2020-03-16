package org.test

class LaClass implements Serializable {

	def script

	LaClass() {

	}

	def callJenkinsSH(cmd) {
		sh("echo 'run $cmd with bash'")
	}

}