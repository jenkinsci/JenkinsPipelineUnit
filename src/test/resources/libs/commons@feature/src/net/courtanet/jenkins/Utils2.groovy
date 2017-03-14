package net.courtanet.jenkins

class Utils2 implements Serializable {

	def script

	Utils2() {

	}

	Utils2(script) {
		this.script = script
	}

	/**
	 * @return GIT config for devteam-tools
	 */
	def gitTools() {
	    return [branch: 'feature']
	}

	static tools() {
		return "something"
	}

}