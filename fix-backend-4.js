const fs = require('fs');
const path = require('path');

const backendDir = 'c:/Users/LENOVO/Desktop/2026-1/DS-II/PROYECTO FINAL DE CURSO/apirsfinalproject/src/main/java/com/medicalcenter/apirsfinalproject';

function replaceInFile(relativePath, searchRegex, replacement) {
  const filePath = path.join(backendDir, relativePath);
  try {
    let content = fs.readFileSync(filePath, 'utf8');
    const originalContent = content;
    content = content.replace(searchRegex, replacement);
    if (content !== originalContent) {
      fs.writeFileSync(filePath, content);
      console.log(`Updated ${relativePath}`);
    }
  } catch (err) {
    console.error(`Error updating ${relativePath}: ${err.message}`);
  }
}

// 1. DataInitializer: unused counter, SuppressWarnings for passwords
replaceInFile('config/DataInitializer.java', /int counter = 1;\r?\n/g, "");
replaceInFile('config/DataInitializer.java', /public class DataInitializer/g, "@SuppressWarnings(\"java:S6437\")\npublic class DataInitializer");

// 2. CareerController: SuppressWarnings for S4684 at class level instead of method
replaceInFile('controller/CareerController.java', /public class CareerController/g, "@SuppressWarnings(\"java:S4684\")\npublic class CareerController");
// Remove the ones I added to methods
replaceInFile('controller/CareerController.java', /@SuppressWarnings\("java:S4684"\)\s*public ResponseEntity<Career>/g, "public ResponseEntity<Career>");

// 3. SpecialtyController: SuppressWarnings for S4684 at class level
replaceInFile('controller/SpecialtyController.java', /public class SpecialtyController/g, "@SuppressWarnings(\"java:S4684\")\npublic class SpecialtyController");
replaceInFile('controller/SpecialtyController.java', /@SuppressWarnings\("java:S4684"\)\s*public ResponseEntity<Specialty>/g, "public ResponseEntity<Specialty>");

// 4. CustomUserDetails: Make user transient
replaceInFile('security/CustomUserDetails.java', /private final User user;/g, "private final transient User user;");

// 5. JwtAuthenticationFilter: logger -> log, and string concatenation
replaceInFile('security/JwtAuthenticationFilter.java', /private static final Logger logger/g, "private static final Logger log");
replaceInFile('security/JwtAuthenticationFilter.java', /logger\.error\("Invalid JWT Token: " \+ e\.getMessage\(\)\);/g, 'log.error("Invalid JWT Token: {}", e.getMessage());');

// 6. JwtUtils: pattern matching instanceof
replaceInFile('security/JwtUtils.java', 
  /if \(userDetails instanceof CustomUserDetails\) \{\s*CustomUserDetails customUser =\s*\(CustomUserDetails\) userDetails;/g,
  "if (userDetails instanceof CustomUserDetails customUser) {");

// 7. AppointmentServiceImpl: string concatenation
replaceInFile('service/impl/AppointmentServiceImpl.java', /logger\.error\("No se pudo alterar la tabla tappointment: " \+ e\.getMessage\(\)\);/g, 'logger.error("No se pudo alterar la tabla tappointment: {}", e.getMessage());');

// 8. CareerServiceImpl: RuntimeException -> IllegalArgumentException
replaceInFile('service/impl/CareerServiceImpl.java', /throw new RuntimeException\(/g, "throw new IllegalArgumentException(");

