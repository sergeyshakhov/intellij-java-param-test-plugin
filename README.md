# IntelliJ Parameterized Test Generator Plugin

## Overview
The **Parameterized Test Generator** plugin for IntelliJ IDEA simplifies the process of creating parameterized tests for your Java methods. With a single action, you can generate a test class with a properly annotated parameterized test method, ensuring all necessary imports are added and optimized.

## Features
- Automatically generates parameterized test method for the selected Java method
- Adds the test method to an existing test class or creates a new one if it doesn't exist
- Ensures required imports are added to the test class
- Optimizes imports to remove unused or redundant entries
- Opens the updated test class in a new editor tab for immediate use

## Development
### Prerequisites
- **JDK**: Version 17 or higher
- **Gradle**: Ensure Gradle is installed or use the Gradle Wrapper included in the project

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
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class CalculatorTest {

   public static Stream<Arguments> testAdd() {
      return Stream.of(
              arguments(1, 2, 3),
              arguments(5, 5, 10),
              arguments(10, -5, 5)
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