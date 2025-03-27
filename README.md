# IntelliJ Parameterized Test Generator Plugin

## Overview
The **Parameterized Test Generator** plugin for IntelliJ IDEA simplifies the process of creating parameterized tests for your Java methods. With a single action, you can generate a test class with a properly annotated parameterized test method, ensuring all necessary imports are added and optimized.

## Development
### Prerequisites
- **JDK**: Version 17 or higher
- **Gradle**: Ensure Gradle is installed or use the Gradle Wrapper included in the project
- **IntelliJ IDEA**: Version 2024.1 or higher

### Building the Plugin
Build the plugin using Gradle:
```bash
./gradlew build
```

### Running the Plugin Locally
To test the plugin in a sandbox environment:
1. Run the following command:
```bash
./gradlew runIde
```
2. A new instance of IntelliJ IDEA will start with the plugin loaded

## Install the plugin
- Open IntelliJ IDEA
- Go to IntelliJ IDEA > Settings > Plugins
- Click the Gear Icon > Install Plugin from Disk
- Navigate to the .zip file in build/distributions and select it
- Restart IntelliJ IDEA to activate the plugin

## Usage
1. Open a Java class in IntelliJ IDEA
2. Right-click and select **Generate > Parameterized Test...** from the context menu
3. Select the methods for which you want parametrized tests to be generated
4. The plugin will:
    - Create the corresponding test class (`<YourClassName>Test`) if it doesn't exist
    - Add a parameterized test methods for the selected methods
    - Add and optimize necessary imports
    - Open the test class in a new editor tab
5. Generated parameterized test methods requires following dependencies to be added to your project:
```kotlin
dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.assertj:assertj-core:3.27.3")
}
```

## Example

### Input: Java Class
```java
public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
}
```

### Generated Test Class
```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class CalculatorTest {

   public static Stream<Arguments> testAdd() {
      return Stream.of(
              arguments(/* add parameters */)
      );
   }

    @ParameterizedTest
    @MethodSource
    void testAdd(int a, int b, int expected) {
        // given
        var instance = new Calculator();
        
        // when
        int result = instance.add(a, b);
        
        // then
        assertThat(result).isEqualTo(expected);
    }
}
```