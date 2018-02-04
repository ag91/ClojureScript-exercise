(ns applicant-test.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [applicant-test.core-test]
   [applicant-test.common-test]))

(enable-console-print!)

(doo-tests 'applicant-test.core-test)
