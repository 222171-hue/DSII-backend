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

// 1. DTOs: Unused imports
replaceInFile('dto/request/AppointmentRequest.java', /import com\.medicalcenter\.apirsfinalproject\.entity\.Specialty;\r?\n/g, "");
replaceInFile('dto/request/UserRegistrationRequest.java', /import com\.medicalcenter\.apirsfinalproject\.entity\.Specialty;\r?\n/g, "");

// 2. Stream.collect(Collectors.toList()) -> Stream.toList()
const servicesToFix = [
  'service/ReportService.java',
  'service/impl/AppointmentServiceImpl.java',
  'service/impl/UserServiceImpl.java'
];
servicesToFix.forEach(f => {
  replaceInFile(f, /\.collect\(Collectors\.toList\(\)\)/g, ".toList()");
});

// 3. GlobalExceptionHandler: [0-9] -> \\d
replaceInFile('exception/GlobalExceptionHandler.java', /\[0-9\]/g, "\\\\d");

// 4. CustomUserDetails: Make user transient
replaceInFile('security/CustomUserDetails.java', /private User user;/g, "private transient User user;");

// 5. JwtUtils: instanceof CustomUserDetails customUser
replaceInFile('security/JwtUtils.java', 
  /if \(principal instanceof CustomUserDetails\) \{\s*CustomUserDetails customUserDetails = \(CustomUserDetails\) principal;/g,
  "if (principal instanceof CustomUserDetails customUserDetails) {");

// 6. SecurityConfig: Remove throws Exception
replaceInFile('security/SecurityConfig.java', /throws Exception /g, "");

// 7. JwtAuthenticationFilter: Primitive boolean expression (L48)
// Need to see the exact code, skipping for manual fix.

