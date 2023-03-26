import libs.H2.h2Test
import libs.H2.h2TestRuntime
import libs.Jasypt.jasypt
import libs.JUnit.jUnit
import libs.MockK.mockKTest
import libs.SpringMockK.springMockKTest
import libs.SpringSecurity.springSecurity

dependencies {
    h2Test()
    h2TestRuntime()
    jasypt()
    jUnit()
    mockKTest()
    springMockKTest()
    springSecurity()
}
