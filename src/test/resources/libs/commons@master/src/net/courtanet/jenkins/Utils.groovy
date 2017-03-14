package net.courtanet.jenkins

@Grab('org.apache.commons:commons-math3:3.4.1')
import org.apache.commons.math3.primes.Primes

class Utils implements Serializable {

	def script

	Utils() {

	}

	Utils(script) {
		this.script = script
	}

	void parallelize(int count) {
		if (!Primes.isPrime(count)) {
			echo "${count} was not prime"
		}
		// …
	}

	/**
	 * @return GIT config for devteam-tools
	 */
	def gitTools() {
	    return [branch: 'master']
	}

}