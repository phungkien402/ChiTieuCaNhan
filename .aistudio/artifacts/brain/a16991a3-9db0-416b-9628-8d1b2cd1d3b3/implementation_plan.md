# Quản Lý Chi Tiêu Cá Nhân & Sổ Nợ Phòng Trọ (ChiTieuPro)

Ứng dụng quản lý tài chính cá nhân toàn diện kết hợp sổ tay chia tiền phòng thông minh, giúp người dùng ghi chép chi thu hàng ngày, tự động tính toán tiền chia đều khi mua đồ ăn chung, theo dõi các khoản nợ cần trả ngay lập tức và đồng bộ dữ liệu an toàn lên đám mây với tài khoản Google.

## User Review & Critical Decisions

> [!IMPORTANT]
> Dưới đây là các quyết định sản phẩm đã được xác nhận qua câu hỏi khảo sát ở Bước 1 và các thiết lập mặc định tối ưu:

- **Đơn vị tiền tệ chính**: Sử dụng định dạng tiền tệ **VNĐ (đ)** chuẩn Việt Nam (ví dụ: `150.000 đ`), hỗ trợ nhập liệu nhanh bằng bàn phím số có phím tắt thêm hàng nghìn (`000`).
- **Cơ chế chia tiền phòng**: Tự động tính toán chia đều chính xác theo danh sách thành viên phòng được tick chọn khi mua đồ ăn/đồ dùng chung, có hiển thị số tiền mỗi người phải trả và lưu chi tiết lịch sử từng lần chia.
- **Lịch nhắc nhở chi tiêu**: Cung cấp tùy chọn linh hoạt trong Cài đặt với khung giờ mặc định lúc 21:00 hàng ngày để người dùng tổng kết chi tiêu buổi tối, cùng thông báo nhắc nhở các khoản nợ sắp hoặc quá hạn.
- **Xác thực & Đồng bộ đám mây**: Sử dụng Firebase Firestore với xác thực tài khoản Google qua Jetpack Credential Manager để bảo vệ dữ liệu cá nhân, hỗ trợ sử dụng mượt mà khi ngoại tuyến (Firestore offline persistence) và tự động đồng bộ khi có mạng.
- **Sao lưu & Xuất dữ liệu**: Hỗ trợ xuất dữ liệu ra file bảng tính CSV (tương thích Microsoft Excel / Google Sheets) và báo cáo in/PDF trực tiếp từ thiết bị.

---

## 1. Overview & Core Concept

- **What It Does**: 
  - Ghi chép thu chi cá nhân theo từng danh mục (Ăn uống, Mua sắm, Tiền trọ, Điện nước, Lương, Khác...).
  - Quản lý thành viên phòng trọ / nhóm bạn và ghi nhận các đợt đi chợ, mua sắm đồ ăn chung: chọn món mua, tổng tiền, đánh dấu thành viên tham gia -> hệ thống tự tính tiền chia đều và cập nhật công nợ của từng thành viên.
  - Theo dõi danh sách nợ khẩn cấp cần thanh toán ngay (urgent debts/payables), đánh dấu trạng thái đã trả/chưa trả, hẹn giờ hạn chót.
  - Thống kê chi tiêu hàng tháng trực quan (biểu đồ tỷ trọng danh mục, hạn mức ngân sách chi tiêu tiết kiệm, phân tích xu hướng thu chi).
  - Tùy chỉnh giao diện Sáng / Tối (Dark Mode) tối ưu dùng ban đêm, giao diện gọn gàng, tốc độ phản hồi tức thì.
- **Target Audience / Persona**: Người đi làm, sinh viên, người ở trọ hoặc ở chung phòng cần một công cụ đơn giản nhưng chuẩn xác để quản lý cả tiền túi cá nhân lẫn các khoản tiền chợ chia phòng mà không sợ nhầm lẫn hay quên nợ.
- **Key Value**: Xóa bỏ hoàn toàn tình trạng "quên ai chưa trả tiền chợ", giảm căng thẳng tài chính phòng trọ, cảnh báo nợ khẩn cấp và duy trì kỷ luật tiết kiệm tiền hàng tháng.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Đăng nhập Google & Khởi tạo (Onboarding & Auth Gate)**:
   - Mở app -> kiểm tra phiên đăng nhập Google. Nếu chưa đăng nhập, hiển thị màn hình chào mừng sang trọng với nút "Đăng nhập bằng Google".
   - Sau khi đăng nhập, hệ thống tự động tải dữ liệu cá nhân từ Firestore hoặc tạo không gian lưu trữ mới theo `userId`.
2. **Trang chủ & Tổng quan (Dashboard)**:
   - Thẻ số dư ví hiện tại, tổng thu/tổng chi trong tháng và thanh tiến độ ngân sách tiết kiệm (Budget Tracker).
   - Danh sách phím tắt thao tác nhanh: `+ Thêm chi tiêu`, `+ Mua đồ chia phòng`, `+ Khoản nợ khẩn cấp`.
   - Danh sách giao dịch gần nhất và widget cảnh báo nhanh nợ khẩn cấp cần trả ngay.
3. **Mục Ghi Chú / Chia Tiền Phòng Trọ (Room Split Ledger)**:
   - Danh sách các thành viên trong phòng (có thể thêm, sửa, xóa thành viên kèm tên, biệt danh, avatar chữ cái).
   - Nút "Thêm đợt mua chung": nhập tiêu đề (vd: "Mua thịt bò, rau củ và gia vị"), ngày mua, tổng tiền chi ra (do chủ app bỏ tiền túi).
   - Bảng checkbox danh sách thành viên: người dùng tick chọn ai tham gia bữa ăn đó -> app lập tức tính `Số tiền mỗi người = Tổng tiền / Số người được chọn`.
   - Lưu lại lịch sử chi tiết; mỗi thành viên có sổ công nợ riêng: "Đang nợ bạn bao nhiêu" / "Đã thanh toán". Có nút "Xác nhận đã trả tiền" để gạch nợ nhanh.
4. **Quản Lý Nợ Khẩn Cấp (Urgent Debts / Payables)**:
   - Phân loại rõ ràng: "Nợ tôi cần trả" (khoản nợ gấp: tiền nhà, tiền điện nước, vay bạn) và "Người khác nợ tôi".
   - Huy hiệu báo động màu đỏ cho các khoản đến hạn hôm nay hoặc quá hạn.
   - Thao tác 1-chạm để đánh dấu "Đã thanh toán xong".
5. **Thống Kê Hàng Tháng & Nhắc Nhở Tiết Kiệm (Analytics & Budget)**:
   - Bộ chọn tháng / năm linh hoạt.
   - Biểu đồ phân bổ tỷ lệ chi tiêu theo nhóm danh mục dạng Canvas vòng cung (Donut Chart) sắc nét, mượt mà.
   - Lời khuyên chi tiêu tiết kiệm thông minh dựa trên tỷ lệ bội chi so với ngân sách đã đặt.
6. **Cài Đặt, Dark Mode, Xuất File & Thông Báo (Settings)**:
   - Công tắc chuyển đổi Light / Dark / Theo hệ thống.
   - Quản lý danh mục Thu - Chi (tùy biến tên, biểu tượng, màu sắc).
   - Cấu hình giờ nhắc nhở chi tiêu (Time Picker) và công tắc bật/tắt nhắc nợ.
   - Nút "Xuất dữ liệu Excel (CSV)" và "Tạo bản in PDF" chia sẻ ra các ứng dụng khác qua Android Sharesheet.

### Visual Identity & Theme

- **Aesthetic Direction**: Minimalist Financial Luxury — Phong cách tối giản hiện đại lấy cảm hứng từ các ứng dụng tài chính hàng đầu (Revolut, Wallet, Notion Finance), đề cao tính rõ ràng của số liệu, viền bo cong mềm mại và độ tương phản cao.
- **Color Palette**:
  - `Primary`: Xanh ngọc đậm sang trọng (Emerald / Deep Teal - `#00685F` cho Light, `#4DDAD1` cho Dark), đại diện cho sự phát triển và cân bằng tài chính.
  - `Secondary / Accent`: Vàng hổ phách cổ điển (Amber `#D97706`), tạo điểm nhấn cho các mục chia tiền và lời khuyên tiết kiệm.
  - `Error / Urgent`: Đỏ san hô sắc nét (`#DC2626` / `#FF897D`) cho các khoản nợ gấp và cảnh báo vượt ngân sách.
  - `Background & Surface`:
    - *Light Mode*: Nền xám ngọc trai dịu mắt (`#F8FAFC`), thẻ nền trắng tinh khôi (`#FFFFFF`) với viền mỏng tinh tế (`#E2E8F0`).
    - *Dark Mode*: Nền than chì sâu (`#0F172A`), thẻ bề mặt nổi (`#1E293B`) với viền phân tách mờ (`#334155`), giúp sử dụng ban đêm không gây mỏi mắt.
- **Typography**: Phông chữ Material 3 hiện đại, kích thước số dư to bản, rõ ràng (`HeadlineLarge`, `TitleMedium`), số liệu căn chỉnh chuẩn mực tabular figures.
- **Component Styling & Layout**: Thẻ M3 nổi bật với elevation nhẹ, góc bo `16.dp` - `20.dp`, các nút bấm có kích thước tối thiểu `48.dp`, các checkbox có diện tích tương tác rộng rãi.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Firestore kèm Offline Persistence cho Đồng Bộ Đám Mây Cá Nhân**:
  - *Chosen Approach*: Lưu trữ toàn bộ dữ liệu người dùng trong Firestore dưới cấu trúc phân cấp `/users/{userId}/...`, đồng thời kích hoạt bộ nhớ đệm ngoại tuyến mặc định của Firestore SDK.
  - *Why*: Đảm bảo không bị mất dữ liệu khi đổi máy hay cài lại app. Khi không có kết nối mạng (offline), người dùng vẫn thêm, sửa, xóa giao dịch tức thì, và Firestore sẽ tự động đồng bộ lên máy chủ ngay khi có Internet.
- **Decision 2: Xác Thực Google Sign-In với Jetpack Credential Manager**:
  - *Chosen Approach*: Sử dụng API hiện đại `CredentialManager` với `GetSignInWithGoogleOption` theo đúng chuẩn bảo mật của Google và chỉ thị hệ thống (không dùng phương thức ẩn danh).
  - *Why*: Đảm bảo danh tính duy nhất của người dùng, liên kết trực tiếp với email Google để phân quyền dữ liệu cá nhân theo quy tắc bảo mật `firestore.rules`.
- **Decision 3: Tính Toán Chia Đều & Lịch Sử Công Nợ Phòng Trọ**:
  - *Chosen Approach*: Khi tạo đợt mua, lưu danh sách thành viên tham gia (`memberIds`), số tiền mỗi người (`splitAmount = totalAmount / memberIds.size`), và đồng thời tạo các bản ghi công nợ con (debt entries).
  - *Why*: Cho phép xem lại lịch sử chi tiết từng lần đi chợ (mua những gì, hết bao nhiêu, những ai ăn) lẫn tổng nợ tích lũy của từng bạn cùng phòng mà không lo sai sót số học.
- **Decision 4: Thông Báo Nhắc Nhở Cục Bộ (AlarmManager / WorkManager)**:
  - *Chosen Approach*: Sử dụng Notification Manager và AlarmManager/WorkManager trên thiết bị Android để đặt lịch thông báo hàng ngày và nhắc nợ mà không cần phụ thuộc vào dịch vụ máy chủ tốn kém.
  - *Why*: Hoạt động ổn định ngay cả khi ngoại tuyến, phản hồi chính xác theo giờ người dùng cài đặt.
- **Decision 5: Xuất Báo Cáo CSV & In Tài Liệu**:
  - *Chosen Approach*: Tạo tệp CSV chuẩn UTF-8 (có BOM để mở đúng tiếng Việt trên Excel) và chia sẻ qua `FileProvider` hoặc Android PrintManager.
  - *Why*: Nhẹ, tương thích 100% với Excel/Google Sheets, không làm tăng dung lượng APK hay gây lỗi phụ thuộc thư viện nặng.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                          Jetpack Compose UI                            │
│  ┌─────────────────┬──────────────────┬─────────────────┬───────────┐  │
│  │ DashboardScreen │  RoomSplitScreen │ UrgentDebtScreen│ StatsView │  │
│  └────────┬────────┴────────┬─────────┴────────┬────────┴─────┬─────┘  │
│           │                 │                  │              │        │
│  ┌────────▼─────────────────▼──────────────────▼──────────────▼─────┐  │
│  │                    StateFlow / ViewModels                         │  │
│  │        (ExpenseViewModel, RoomSplitViewModel, DebtViewModel)       │  │
│  └──────────────────────────────┬────────────────────────────────────┘  │
│                                 │                                       │
│  ┌──────────────────────────────▼────────────────────────────────────┐  │
│  │                     Data & Repository Layer                       │  │
│  │     - ExpenseRepository          - RoomSplitRepository            │  │
│  │     - UrgentDebtRepository       - UserSettingsRepository         │  │
│  └───────────────┬──────────────────────────────┬────────────────────┘  │
│                  │                              │                       │
│  ┌───────────────▼──────────────┐  ┌────────────▼────────────────────┐  │
│  │    Firebase Firestore SDK    │  │   Android System Services       │  │
│  │ (users/{uid}/transactions)   │  │   - CredentialManager (Google)  │  │
│  │ (users/{uid}/room_members)   │  │   - NotificationManager / Alarm │  │
│  │ (users/{uid}/room_splits)    │  │   - FileProvider (Export CSV)   │  │
│  │ (users/{uid}/urgent_debts)   │  │   - Local DataStore Preferences │  │
│  └──────────────────────────────┘  └─────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

### Data Entities & Schema

1. **Transaction / Expense (`/users/{uid}/transactions/{id}`)**:
   - `id`: String (Document ID)
   - `title`: String (Tên khoản chi / thu)
   - `amount`: Double (Số tiền)
   - `type`: String (`"EXPENSE"` hoặc `"INCOME"`)
   - `category`: String (Ăn uống, Tiền nhà, Mua sắm, Di chuyển, Lương, Khác...)
   - `note`: String (Ghi chú thêm)
   - `timestamp`: Timestamp (Thời gian phát sinh)
   - `createdAt`, `updatedAt`: Timestamp (Server timestamps)
2. **Room Member (`/users/{uid}/room_members/{id}`)**:
   - `id`: String
   - `name`: String (Tên bạn cùng phòng)
   - `phone`: String (Số điện thoại / liên hệ)
   - `avatarColor`: Long (Mã màu hiển thị)
   - `totalOwed`: Double (Tổng số tiền bạn này đang nợ chủ app)
   - `createdAt`, `updatedAt`: Timestamp
3. **Room Split Expense (`/users/{uid}/room_splits/{id}`)**:
   - `id`: String
   - `title`: String (Tên đợt mua đồ ăn / đồ dùng, ví dụ: "Tiền chợ trưa")
   - `totalAmount`: Double (Tổng tiền thanh toán)
   - `payerId`: String (ID người chi tiền - mặc định là chủ app)
   - `participantIds`: List<String> (Danh sách ID những người tham gia ăn / chia)
   - `splitAmountPerPerson`: Double (Tiền mỗi người = `totalAmount / participantIds.size()`)
   - `paidMemberIds`: List<String> (Danh sách những người đã hoàn tiền cho bạn)
   - `date`: Timestamp
   - `createdAt`, `updatedAt`: Timestamp
4. **Urgent Debt (`/users/{uid}/urgent_debts/{id}`)**:
   - `id`: String
   - `title`: String (Tên khoản nợ cần trả gấp, ví dụ: "Tiền phòng tháng này", "Trả nợ anh Nam")
   - `amount`: Double
   - `isPayable`: Boolean (`true`: Tôi nợ người khác, `false`: Người khác nợ tôi)
   - `contactName`: String (Tên chủ nợ / người nợ)
   - `dueDate`: Timestamp (Hạn chót thanh toán)
   - `isSettled`: Boolean (Đã trả xong hay chưa)
   - `createdAt`, `updatedAt`: Timestamp
5. **Budget & Preferences (`/users/{uid}/settings/preferences`)**:
   - `monthlyBudget`: Double (Hạn mức chi tiêu tháng mục tiêu)
   - `reminderTimeHour`: Int (Giờ nhắc nhở hàng ngày, mặc định 21)
   - `reminderTimeMinute`: Int (Phút nhắc nhở, mặc định 0)
   - `isReminderEnabled`: Boolean
   - `isDarkMode`: Boolean / System

### Step-by-Step Implementation Sequence

1. **Step 1: Firebase Provisioning & Security Rules**:
   - Khởi tạo quy trình `set_up_firebase` với `"firestore"` để cấp phát backend và Google Auth.
   - Viết `firebase-blueprint.json` và `firestore.rules` đảm bảo quy tắc zero-trust nghiêm ngặt: chỉ người dùng sở hữu dữ liệu (`request.auth.uid == userId`) mới có quyền Đọc/Ghi.
   - Chạy kiểm thử rules tự động và deploy lên Firebase với `deploy_firebase`.
2. **Step 2: Authentication & Core Dependencies**:
   - Cấu hình dependencies: `firebase-firestore`, `firebase-auth`, `androidx.credentials`, `googleid`.
   - Cập nhật `app_name` trong `strings.xml` và `name` trong `metadata.json` đồng bộ thành "Chi Tiêu Pro".
   - Triển khai `GoogleSignInButton` và luồng xác thực Google mượt mà với Credential Manager, kèm màn hình Welcome & Sign-In ấn tượng.
3. **Step 3: Repositories & Offline Data Layer**:
   - Xây dựng `ExpenseRepository`, `RoomSplitRepository`, `UrgentDebtRepository` với cơ chế lắng nghe thời gian thực (`snapshots()`) kết hợp cache ngoại tuyến của Firestore.
4. **Step 4: Màn Hình Nghiệp Vụ Chính (Jetpack Compose M3)**:
   - **Dashboard**: Thẻ số dư nổi bật, biểu đồ tiến độ ngân sách, danh sách giao dịch gần đây.
   - **Room Split (Sổ nợ phòng trọ)**: Quản lý danh viên phòng, form ghi nhận đi chợ với checkbox chia đều tức thì, chi tiết lịch sử từng lần mua, nút gạch nợ từng người.
   - **Urgent Debts**: Danh sách nợ khẩn cấp cần giải quyết ngay, cờ đánh dấu quá hạn/đến hạn, bộ lọc Đã trả / Chưa trả.
   - **Analytics & Report**: Biểu đồ hình tròn M3 vẽ bằng Canvas phân bổ danh mục, phân tích thu chi, mẹo tiết kiệm.
   - **Export & Settings**: Tạo file CSV tiếng Việt UTF-8 chuẩn xác, trình chọn giờ nhắc nhở, chuyển đổi Dark/Light mode tức thì.
5. **Step 5: Notifications & Android Integration**:
   - Cấp quyền `POST_NOTIFICATIONS` cho Android 13+.
   - Thiết lập `ExpenseReminderReceiver` gửi thông báo nhắc nhở chi tiêu lúc 21h hoặc theo giờ người dùng tùy chỉnh.
6. **Step 6: Build & Verification**:
   - Chạy `compile_applet` để xác minh ứng dụng biên dịch hoàn hảo, không có cảnh báo hay lỗi cú pháp.
