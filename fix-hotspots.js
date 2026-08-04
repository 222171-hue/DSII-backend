const fs = require('fs');
const path = require('path');
const controllers = [
  'SpecialtyController.java', 'UserController.java', 'NotificationController.java',
  'CareerController.java', 'AuthController.java', 'AppointmentController.java'
];
const backendDir = 'c:/Users/LENOVO/Desktop/2026-1/DS-II/PROYECTO FINAL DE CURSO/apirsfinalproject/src/main/java/com/medicalcenter/apirsfinalproject';

controllers.forEach(c => {
  const file = path.join(backendDir, 'controller', c);
  let content = fs.readFileSync(file, 'utf8');
  content = content.replace(/@CrossOrigin\(origins = "\*"\)/g, '@CrossOrigin(origins = "http://localhost:4200")');
  fs.writeFileSync(file, content);
  console.log('Fixed ' + c);
});

const securityConfigPath = path.join(backendDir, 'security', 'SecurityConfig.java');
let secContent = fs.readFileSync(securityConfigPath, 'utf8');
secContent = secContent.replace(/configuration\.setAllowedOriginPatterns\(java\.util\.List\.of\("\*"\)\);/g, 'configuration.setAllowedOriginPatterns(java.util.List.of("http://localhost:4200"));');
fs.writeFileSync(securityConfigPath, secContent);
console.log('Fixed SecurityConfig.java');
