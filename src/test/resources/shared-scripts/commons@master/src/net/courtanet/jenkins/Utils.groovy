package net.courtanet.jenkins

class Utils implements Serializable {

	def script

	Utils() {

	}

	Utils(script) {
		this.script = script
	}

	/**
	 * @return GIT config for devteam-tools
	 */
	def gitTools() {
	    return [branch: 'master']
	}

}