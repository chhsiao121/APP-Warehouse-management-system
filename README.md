倉儲盤點APP
=============================

<p float="left">
  <img src="../../blob/main/5.jpg?raw=true" width="250" />
  <img src="../../blob/main/4.jpg?raw=true" width="250" /> 
  <img src="../../blob/main/3.jpg?raw=true" width="250" />
  <img src="../../blob/main/2.jpg?raw=true" width="250" />
  <img src="../../blob/main/1.jpg?raw=true" width="250" />
</p>



簡介
------------

- 使用者註冊、登入系統
- 查看、新增盤點資料
- 使用者可依多種預設項目填寫
- 資料可附加手機內照片
- 資料庫內容會於手機連線時自動同步至遠端資料庫

使用套件
------------

- [Firebase Database](https://firebase.google.com/docs/database)

資料庫架構
------------
- 圖片已JPEG格式儲存於firebase storeage，檔名格式為`時間湊雜碼.jpg`,如`1628241139810.jpg`
- 倉儲資料已JSON格式儲存於firebase realtime database，JSON root 目錄中分為`post`及`user`子項
 - `user`用於儲存使用者資料，格式如下
```
{
    "使用者唯一id(英數混合30碼)" : {
      "email" : "使用者註冊email",
      "username" : "使用者註冊名"
    },

    "6zQSr4KCy9fX4pXW4ThRtYMMJRS2" : {
      "email" : "user123@gmail.com",
      "username" : "user123"
    }
}
```
 - `post`用於儲存倉儲資料，格式如下
```
    "倉儲資料識別碼" : {
      "author" : "編輯者id",
      "count" : "盤點數量",
      "format" : "單位",
      "location" : "位置",
      "name" : "品名",
      "number" : "帳上數量",
      "remarks" : "註記",
      "snumber" : "te7",
      "starCount" : 0,
      "uid" : "編輯者id",
      "unit" : "單位",
      "uploadFileName" : "照片id"
    },

    "-MgPj0IPPqCv51pcL4Y2" : {
      "author" : "power703",
      "count" : "4",
      "format" : "個",
      "location" : "i8",
      "name" : "熊",
      "number" : "5",
      "remarks" : "",
      "snumber" : "te7",
      "starCount" : 0,
      "uid" : "hMGvtJcv6bbb1znwvINRzcZZ7fj1",
      "unit" : "個",
      "uploadFileName" : "1628241139810.jpg"
    },
```

展示影片
------------
[Youtube連結](https://youtu.be/mqGtv7kGnhU)

預計增加功能
------------
 - [ ] 制定目錄樹分類（倉庫、區、排、位置）
 - [ ] 可自訂項目欄位模板（目前是預設）
 - [ ] 可附加多張照片
 - [ ] beta版上架play商店
