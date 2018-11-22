# Atmon_Mobile
An attendance taking mobile application for student 

```
Mobile Application build together with web application (Attendance Monitoring).
```

```
Flow :
1. Student goes to a classroom and attend their class.
2. Student connect their smartphone to the given access points.
3. Student open the app to take attendance.
    - The app will show the information of current ongoing class (registered at the begining on the semester)
    - This means student do not have to choose anything, just a click on confirmation button.
    - The app will only allow students to take attendance when connecting to the given access point. (verified using mac address)
    - Admins can assign any classroom to any access point in the web app.
      (e.g. Block A access point - classroom A100, A101, A102...)
    - This is to prevent students taking attendance outside of their class.
4. Student can check their attendance history anytime anywhere without being required to connect to the institute access point.
    - 2 main modules :
      a. Take attendance: student must connect to the registered access points to take attendance
      b. Check attendance: student can check their history anytime anywhere as long as their are connected to internet
5. Admins and lecturers can check students attendances with web app (https://github.com/qhaixan/Atmon_Web).
    - Check based on subject and all students who are taking it
    - Check all classes attended by any student and their attendance rate
    - Graph of attendance rate
    
Objective:
- remove paper work
- remove manual work
- minimize chance of faking attendance by signin on behalf of other student
  (The app will bind with student id once, even if students clear app cache, the system will check for mobile mac address. Means single address are not allowed to take multiple attendance.)
- no waiting to pass around the attendance sheet
