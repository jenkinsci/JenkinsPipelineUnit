package org.example.testsharedlib

import com.lesfurets.jenkins.unit.cps.PipelineTestHelperCPS
import com.lesfurets.jenkins.unit.PipelineTestHelper

import com.cloudbees.groovy.cps.impl.CpsCallableInvocation
import com.cloudbees.groovy.cps.Envs
import com.cloudbees.groovy.cps.Continuation

@groovy.transform.InheritConstructors
class ModPipelineTestHelperCPS extends PipelineTestHelperCPS {
  @Override
  Object callClosure(Closure closure, Object[] args = null) {
      try {
          // MOD: Just to make sure we will execute PipelineTestHelper method
          this.callClosure2(closure, args)
      } catch(CpsCallableInvocation e) {
          def next = e.invoke(Envs.empty(), null, Continuation.HALT)
          while(next.yield==null) {
              // MOD: Removed custom exception to show better info
              try {
                  this.roundtripSerialization(next.e)
              } catch (NotSerializableException exception) {
                  def cls = exception.getMessage()
                  def vars = next.e.locals.findAll { k, v -> v.class.name == cls }
                  vars = vars.isEmpty() ? 'UNKNOWN' : vars
                  throw new NotSerializableException("Unable to serialize locals ${vars} in `${next.e.closureOwner().class.name}`", exception)
              } catch (exception) {
                  throw new Exception("Exception during serialization of locals ${next.e.locals} in `${next.e.closureOwner().class.name}`", exception)
              }
              next = next.step()
          }
          return next.yield.replay()
      }
  }

  Object callClosure2(Closure closure, Object[] args = null) {
      if (!args) {
          return closure.call()
      } else if (args.size() > closure.maximumNumberOfParameters) {
          return closure.call(args)
      } else {
          return closure.call(*args)
      }
  }
}
