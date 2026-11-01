import os
import shutil
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE
from PIL import Image, ImageDraw, ImageFont

BASE_DIR = os.path.abspath(os.path.dirname(__file__))
IMG_DIR = os.path.join(BASE_DIR, "ppt_images")
os.makedirs(IMG_DIR, exist_ok=True)
LOGO_PATH = os.path.join(BASE_DIR, "frontend", "public", "logo.png")

# Color palette
C_DARK_BG = (8, 13, 22)
C_CARD_BG = (16, 24, 39)
C_CARD_BG2 = (21, 31, 48)
C_CYAN = (8, 184, 237)
C_CYAN_LIGHT = (56, 189, 248)
C_RED = (244, 63, 94)
C_WHITE = (248, 250, 252)
C_GRAY = (148, 163, 184)
C_DARK_GRAY = (71, 85, 105)
C_GREEN = (16, 185, 129)
C_BORDER = (30, 41, 59)

PPT_DARK_BG = RGBColor(8, 13, 22)
PPT_CARD_BG = RGBColor(16, 24, 39)
PPT_CYAN = RGBColor(8, 184, 237)
PPT_CYAN_LIGHT = RGBColor(56, 189, 248)
PPT_WHITE = RGBColor(248, 250, 252)
PPT_GRAY = RGBColor(148, 163, 184)
PPT_GREEN = RGBColor(16, 185, 129)
PPT_BORDER = RGBColor(30, 41, 59)
PPT_RED = RGBColor(244, 63, 94)

def get_font(size, bold=False):
    paths = [
        "C:\\Windows\\Fonts\\segoeuib.ttf" if bold else "C:\\Windows\\Fonts\\segoeui.ttf",
        "C:\\Windows\\Fonts\\arialbd.ttf" if bold else "C:\\Windows\\Fonts\\arial.ttf",
        "C:\\Windows\\Fonts\\calibrib.ttf" if bold else "C:\\Windows\\Fonts\\calibri.ttf"
    ]
    for p in paths:
        if os.path.exists(p):
            try:
                return ImageFont.truetype(p, size)
            except Exception:
                pass
    return ImageFont.load_default()

def generate_mockup(filename, category, title, items, status_color=C_CYAN, extra_note=""):
    w, h = 960, 620
    im = Image.new("RGBA", (w, h), C_DARK_BG)
    draw = ImageDraw.Draw(im)

    # Card background
    draw.rounded_rectangle([(15, 15), (w - 15, h - 15)], radius=18, fill=C_CARD_BG, outline=C_BORDER, width=2)

    # Top Window Bar
    draw.rounded_rectangle([(30, 30), (w - 30, 95)], radius=12, fill=C_CARD_BG2, outline=(40, 55, 80), width=1)
    
    # 3 traffic lights
    draw.ellipse([(45, 55), (60, 70)], fill=(239, 68, 68))
    draw.ellipse([(70, 55), (85, 70)], fill=(234, 179, 8))
    draw.ellipse([(95, 55), (110, 70)], fill=(34, 197, 94))

    # Category Pill
    pill_w = 190
    draw.rounded_rectangle([(130, 45), (130 + pill_w, 80)], radius=8, fill=(status_color[0]//4, status_color[1]//4, status_color[2]//4), outline=status_color, width=1)
    draw.text((145, 52), category.upper(), font=get_font(14, bold=True), fill=status_color)

    # Title in top bar
    draw.text((345, 51), title, font=get_font(20, bold=True), fill=C_WHITE)

    # Items container
    y = 120
    for it_title, it_desc, it_badge, it_type in items:
        box_h = 75
        draw.rounded_rectangle([(30, y), (w - 30, y + box_h)], radius=12, fill=C_CARD_BG2, outline=(35, 50, 75), width=1)
        
        # Indicator strip
        color_strip = C_GREEN if it_type == 'green' else (C_RED if it_type == 'red' else C_CYAN)
        draw.rounded_rectangle([(40, y + 14), (47, y + box_h - 14)], radius=3, fill=color_strip)

        # Texts
        draw.text((65, y + 13), it_title, font=get_font(18, bold=True), fill=C_WHITE)
        draw.text((65, y + 42), it_desc, font=get_font(15), fill=C_GRAY)

        # Right badge
        if it_badge:
            b_width = 170
            bx = w - 45 - b_width
            by = y + 20
            bg_badge = (16, 45, 30) if it_type == 'green' else ((50, 20, 25) if it_type == 'red' else (15, 35, 60))
            text_badge_color = C_GREEN if it_type == 'green' else (C_RED if it_type == 'red' else C_CYAN_LIGHT)
            draw.rounded_rectangle([(bx, by), (bx + b_width, by + 35)], radius=8, fill=bg_badge, outline=text_badge_color, width=1)
            draw.text((bx + 12, by + 8), it_badge, font=get_font(13, bold=True), fill=text_badge_color)

        y += box_h + 14

    # Bottom footer info
    if extra_note:
        draw.rounded_rectangle([(30, h - 60), (w - 30, h - 25)], radius=8, fill=(12, 18, 30), outline=(30, 45, 65), width=1)
        draw.text((45, h - 50), extra_note, font=get_font(13), fill=C_CYAN_LIGHT)

    filepath = os.path.join(IMG_DIR, filename)
    im.save(filepath, "PNG")
    return filepath

print("Generating 20 slide visual images...")

# 1. Title Image
generate_mockup(
    "img_01_hero.png", "Enterprise Stack", "KLMedia Social Platform Overview",
    [
        ("Frontend Architecture", "React 18 + Vite SPA with Dark Ocean Glassmorphism Theme", "Active v1.0", "green"),
        ("Backend Services", "Spring Boot 3.4.5 + Java 17 + Spring Security + REST APIs", "Live Port 8080", "green"),
        ("Database & Persistence", "MySQL 8 Relational Database with JPA Hibernate ORM", "7 Core Tables", "cyan"),
        ("Security & Messaging", "Stateless JWT (HMAC-SHA384) + JavaMailSender SMTP Engine", "TLS 587 Ready", "cyan"),
        ("Social Capabilities", "Feed, Likes, Comments, Search, Follows, Profiles & Direct Chat", "Fully Integrated", "green")
    ],
    status_color=C_CYAN,
    extra_note="• Complete Full-Stack Solution Developed for Modern Web Performance"
)

# 2. Architecture Overview
generate_mockup(
    "img_02_arch.png", "System Blueprint", "Multi-Tier Enterprise Architecture",
    [
        ("Client Tier (React 18)", "Vite build, Axios with Authorization Bearer Interceptor, Lucide UI", "Browser Client", "cyan"),
        ("Security Filter Tier", "JwtAuthFilter, OncePerRequestFilter, BCryptPasswordEncoder", "Zero-Trust", "green"),
        ("Controller & Service Tier", "AuthController, PostService, FollowService, MessageService", "REST Endpoints", "cyan"),
        ("Persistence Tier", "Spring Data JPA Repositories executing on MySQL 8.0 Engine", "ACID Compliant", "green"),
        ("Notification & Mailer", "JavaMailSender SMTP dispatching branded HTML emails", "Gmail SMTP 587", "cyan")
    ],
    status_color=C_CYAN_LIGHT,
    extra_note="• Strict separation of concerns with Stateless JWT Session Management"
)

# 3. Security Framework
generate_mockup(
    "img_03_security.png", "Defense in Depth", "Spring Security & Token Protection",
    [
        ("Stateless JWT Tokens", "HMAC-SHA384 signed tokens with 24-hour expiration window", "HMAC-SHA384", "green"),
        ("Password Encryption", "BCrypt salted password hashing with 10 rounds complexity", "One-Way Hash", "green"),
        ("Verification Code Hash", "6-digit OTP codes stored strictly as hashes in MySQL database", "Zero Plaintext", "green"),
        ("Rate Limiter Cooldown", "60-second cooldown per email for verification and reset requests", "Anti-Abuse", "cyan"),
        ("Cross-Origin Resource Sharing", "Strict CORS configuration for localhost:5173 & localhost:5175", "Secured Gateway", "cyan")
    ],
    status_color=C_GREEN,
    extra_note="• Zero-Trust Architecture: Authentication headers validated per request"
)

# 4. Sign In Page
generate_mockup(
    "img_04_signin.png", "Authentication", "Sign In Page & Dual Role Access",
    [
        ("Dual-Role Toggle Tab", "Seamless switching between Normal User Login & Admin Login", "User vs Admin", "cyan"),
        ("Credential Validation", "Validates registered email address and BCrypt password hash", "Instant Auth", "green"),
        ("Password Visibility Toggle", "Show/Hide password feature using dynamic Lucide Eye icons", "Enhanced UX", "cyan"),
        ("Unverified Account Guard", "Blocks login if account is unverified with 'Verify Now' prompt", "Account Guard", "red"),
        ("Forgot Password Link", "Direct navigation to the 2-step account recovery module", "Direct Link", "cyan")
    ],
    status_color=C_CYAN,
    extra_note="• Endpoint: POST /api/auth/login | Token stored securely in Frontend"
)

# 5. Sign Up Page
generate_mockup(
    "img_05_signup.png", "Registration", "User Sign Up & Input Validation",
    [
        ("Comprehensive Form", "Username, Email, Password, and Confirm Password fields", "4 Required Inputs", "cyan"),
        ("Strict Validation Engine", "Email format validation, minimum 6 characters password strength", "Regex & Length", "green"),
        ("Duplicate Detection", "Rejects existing verified email addresses and taken usernames", "Unique Index", "red"),
        ("Pending Activation State", "Saves user with emailVerified=false until email verification", "Pending State", "cyan"),
        ("Immediate OTP Dispatch", "Automatically generates secure 6-digit verification code", "Auto Triggered", "green")
    ],
    status_color=C_CYAN_LIGHT,
    extra_note="• Endpoint: POST /api/auth/register | Transition to verification screen"
)

# 6. Email Verification
generate_mockup(
    "img_06_verify.png", "Email Verification", "6-Digit Code Activation Engine",
    [
        ("Cryptographic Generator", "SecureRandom generates 6-digit OTP (100000 - 999999)", "SecureRandom", "green"),
        ("5-Minute Expiration", "Code expires exactly 5 minutes after issuance in MySQL", "Time-To-Live", "red"),
        ("Branded HTML Email", "Dark ocean card template, KLU branding, and security notices", "JavaMailSender", "cyan"),
        ("60s Cooldown Timer", "Resend button disables with active countdown timer (60s)", "Anti-Spam", "cyan"),
        ("Instant Feed Redirect", "Auto-authenticates on code match and redirects to home feed", "One-Step Entry", "green")
    ],
    status_color=C_GREEN,
    extra_note="• Endpoint: POST /api/auth/verify-email | One-time use enforced"
)

# 7. Forgot Password
generate_mockup(
    "img_07_forgot.png", "Account Recovery", "Forgot Password Request Flow",
    [
        ("Registered Email Input", "User enters their account email to initiate password recovery", "Email Entry", "cyan"),
        ("Privacy Protection", "Returns generic success message to prevent user enumeration", "Anti-Scraping", "green"),
        ("Reset Code Generation", "Generates secure 6-digit reset code saved as BCrypt hash", "Hashed Code", "green"),
        ("HTML Password Reset Email", "Dispatches security instructions and 6-digit code via SMTP", "Dispatched", "cyan"),
        ("Seamless Step 2 Switch", "Smoothly transitions user to reset password verification view", "Step 2 Switch", "cyan")
    ],
    status_color=C_CYAN,
    extra_note="• Endpoint: POST /api/auth/forgot-password | Rate-limited with 60s cooldown"
)

# 8. Reset Password
generate_mockup(
    "img_08_reset.png", "Credential Reset", "Password Reset & Invalidation Engine",
    [
        ("Reset Code Validation", "POST /api/auth/verify-reset-code verifies code match & expiry", "Code Check", "green"),
        ("New Password Entry", "New Password and Confirm Password with show/hide toggles", "Match Check", "cyan"),
        ("BCrypt Re-Hashing", "Re-hashes new password using BCrypt before persisting in DB", "BCrypt Updated", "green"),
        ("Session Invalidation", "Clears resetCodeHash, resetCodeExpiry, and resets flags", "Session Cleared", "red"),
        ("Success & Redirect", "Shows success banner and auto-redirects to login within 2s", "Auto Redirect", "green")
    ],
    status_color=C_GREEN,
    extra_note="• Endpoint: POST /api/auth/reset-password | Invalidation prevents replay"
)

# 9. Admin Dashboard
generate_mockup(
    "img_09_admin.png", "Governance", "Admin Dashboard & Control Console",
    [
        ("Role Authorization", "Protected with Spring Security hasRole('ADMIN') rule", "ADMIN Only", "red"),
        ("Live User Counters", "Total Registered Users, Normal Users, and System Admins", "Real-Time Stats", "green"),
        ("Community Overview", "Dedicated panel for user base auditing and platform health", "Platform View", "cyan"),
        ("Integrated Topbar", "Admin branding, quick logout, and navigation shortcut", "Topbar Control", "cyan"),
        ("Clean Glassmorphism UI", "Ocean dark theme with high-contrast administrative badges", "Modern UI", "cyan")
    ],
    status_color=C_RED,
    extra_note="• Route: /admin | Access denied for standard USER accounts"
)

# 10. User Add & Delete Role
generate_mockup(
    "img_10_user_crud.png", "User Management", "Add & Delete User Operations",
    [
        ("Direct User Creation", "Admin can register new community users directly from dashboard", "POST /api/admin/users", "green"),
        ("Input Validation", "Validates username, unique email, and minimum 6 chars password", "Validation Ready", "cyan"),
        ("User Deletion Action", "Admin can delete user accounts with confirmation modal", "DELETE /api/admin/users/{id}", "red"),
        ("Root Admin Protection", "Hardened check prevents deletion of administrator accounts", "Self-Protect Guard", "red"),
        ("Instant Table Refresh", "Table re-fetches automatically upon creation or deletion", "Sync State", "green")
    ],
    status_color=C_CYAN,
    extra_note="• Dedicated Admin Controller endpoints with full audit logging"
)

# 11. User Home Feed
generate_mockup(
    "img_11_feed.png", "Social Feed", "User Home Feed & Content Stream",
    [
        ("Chronological Feed", "Fetches dynamic community posts ordered by creation date", "GET /api/posts", "green"),
        ("Post Author Header", "Displays author username, avatar initial, and relative time", "Author Metadata", "cyan"),
        ("Visual Post Media", "Displays high-resolution image embeds with responsive aspect ratio", "Media Display", "cyan"),
        ("Caption & Text", "Formatted caption text with typography optimized for readability", "Rich Text", "cyan"),
        ("Integrated Action Bar", "Like button, comment counter, and owner delete options", "Quick Action", "green")
    ],
    status_color=C_CYAN_LIGHT,
    extra_note="• Route: / | Responsive single-column feed layout with smooth scroll"
)

# 12. Create Post Page
generate_mockup(
    "img_12_create.png", "Content Creation", "Create Post & Media Sharing",
    [
        ("Textarea Caption Input", "Expressive text entry with multi-line expandability", "Rich Caption", "cyan"),
        ("Image URL Support", "Embed external and cloud image URLs directly into feed", "Image Linking", "cyan"),
        ("Live Preview & Status", "Loading states and button feedback while post publishes", "Instant Feedback", "green"),
        ("Feed Auto-Insertion", "Newly created post prepends dynamically to the top of feed", "Prepend Update", "green"),
        ("Post Ownership Rights", "Creator is granted ownedByMe=true flag for post management", "Ownership Guard", "cyan")
    ],
    status_color=C_CYAN,
    extra_note="• Endpoint: POST /api/posts | Validated with @Valid @RequestBody"
)

# 13. Engagement: Likes & Comments
generate_mockup(
    "img_13_engage.png", "Interactions", "Engagement Engine: Likes & Comments",
    [
        ("Atomic Toggle Like", "POST /api/posts/{id}/like toggles like state in post_likes table", "Atomic Toggle", "green"),
        ("Like Counter & State", "Heart icon lights up in vibrant cyan/pink with live count", "Liked State", "red"),
        ("Threaded Comments List", "GET /api/posts/{id}/comments loads threaded discussions", "Nested List", "cyan"),
        ("Real-Time Comment Entry", "POST /api/posts/{id}/comments adds instant commentary", "Instant Post", "green"),
        ("Author Attribution", "Commenters identified with username, avatar, and timestamp", "User Linked", "cyan")
    ],
    status_color=C_RED,
    extra_note="• Prevents duplicate likes using composite key in post_likes table"
)

# 14. User Search Page
generate_mockup(
    "img_14_search.png", "Discovery", "User Discovery & Search Engine",
    [
        ("Real-Time Search Input", "Interactive search box with live debounced querying", "Instant Query", "cyan"),
        ("Fuzzy Substring Search", "Searches database usernames matching query text", "GET /api/users/search", "green"),
        ("Self-Exclusion Filter", "Automatically filters out current logged-in user from results", "Smart Filter", "green"),
        ("User Result Cards", "Shows avatar, username, bio snippet, and total post count", "Profile Card", "cyan"),
        ("Quick Profile Link", "Clicking card navigates directly to user's public profile page", "Deep Linking", "cyan")
    ],
    status_color=C_CYAN_LIGHT,
    extra_note="• Route: /search | Limit 20 results for optimal response latency"
)

# 15. User Profile Page
generate_mockup(
    "img_15_profile.png", "User Profiles", "Personal & Public Profile Pages",
    [
        ("Profile Header", "Avatar badge, username, custom bio, and member badges", "Header Showcase", "cyan"),
        ("Social Metrics", "Live counters for Posts, Followers, and Following accounts", "Live Counters", "green"),
        ("Follow / Unfollow CTA", "Dynamic button for following and unfollowing other profiles", "Action CTA", "green"),
        ("User Posts Gallery", "Grid view of all posts published by this specific user", "3-Column Grid", "cyan"),
        ("Private Profile View", "Edit bio, view personal account status, and manage posts", "/profile View", "cyan")
    ],
    status_color=C_CYAN,
    extra_note="• Routes: /profile (Private) & /profile/:id (Public view)"
)

# 16. Followers & Following
generate_mockup(
    "img_16_follow.png", "Social Graph", "Followers & Following Network",
    [
        ("Relational Follow Graph", "follows table maps follower_id to followed_id efficiently", "Entity Graph", "green"),
        ("Follow State Toggle", "POST /api/follow/{id} creates or deletes relationship", "Mutual Toggle", "green"),
        ("Following State Check", "FollowService checks if auth user is currently following", "isFollowing Boolean", "cyan"),
        ("Count Aggregation", "countByFollowedId and countByFollowerId JPA queries", "Indexed Counts", "cyan"),
        ("Auto Follow Notification", "Following a user automatically generates an unread notification", "Event Triggered", "green")
    ],
    status_color=C_CYAN_LIGHT,
    extra_note="• Asymmetric social graph allowing independent follow subscriptions"
)

# 17. Notification Page
generate_mockup(
    "img_17_notifications.png", "Activity Center", "Live Notification Management",
    [
        ("Notification Center", "Unified activity stream for Likes, Comments, and Follows", "Notification Stream", "cyan"),
        ("Unread Counter Badge", "Sidebar badge displays real-time unread notification count", "Badge Counter", "red"),
        ("Unread Highlighting", "Unread notifications glow with cyan border and accent tag", "Visual Contrast", "green"),
        ("Mark as Read Engine", "PATCH /api/notifications/mark-read marks alerts as viewed", "Status Updated", "green"),
        ("Delete Notification", "DELETE /api/notifications/{id} removes dismissable items", "Clear Action", "cyan")
    ],
    status_color=C_RED,
    extra_note="• Route: /notifications | Real-time polling keeps badge updated"
)

# 18. Messenger & Direct Chat
generate_mockup(
    "img_18_messenger.png", "Communication", "Messenger & Direct Chat System",
    [
        ("Direct Message Inbox", "Conversation list displaying all ongoing chat threads", "Thread List", "cyan"),
        ("User Search in Chat", "Search fellow community members to start direct dialogues", "Quick Connect", "cyan"),
        ("Real-Time Message View", "Chronological chat history with styled speech bubbles", "Chat Bubbles", "green"),
        ("Message Composer", "Input box with enter-to-send support and send icon button", "Send Action", "green"),
        ("Messenger Header", "Active partner name, avatar, and conversation metadata", "Partner Header", "cyan")
    ],
    status_color=C_CYAN,
    extra_note="• Route: /messenger | Fast direct messaging between verified users"
)

# 19. Database Schema & Models
generate_mockup(
    "img_19_db.png", "Relational Design", "MySQL Schema & Entity Architecture",
    [
        ("users", "id, username, email, password, role, email_verified, codes, bio", "Primary Table", "green"),
        ("posts", "id, user_id (FK), caption, image_url, created_at", "Content Table", "cyan"),
        ("post_comments & likes", "post_id (FK), user_id (FK), content, created_at", "Interactions", "cyan"),
        ("follows", "id, follower_id (FK), followed_id (FK), created_at", "Social Graph", "green"),
        ("notifications & messages", "recipient_id (FK), sender_id (FK), content, read_status", "Real-Time Comms", "cyan")
    ],
    status_color=C_GREEN,
    extra_note="• Normalized database design with foreign keys and unique constraints"
)

# 20. Testing & Conclusion
generate_mockup(
    "img_20_test.png", "Quality Assurance", "12-Step End-to-End Verification",
    [
        ("Authentication Matrix", "Registration, 6-digit OTP, Expiration & BCrypt Login tested", "100% Passed", "green"),
        ("Account Recovery Matrix", "Forgot password, email OTP delivery & reset tested", "100% Passed", "green"),
        ("RBAC Matrix", "USER vs ADMIN separation and admin user management tested", "100% Passed", "green"),
        ("Social Features Matrix", "Posts, Likes, Comments, Follows & Search tested", "100% Passed", "green"),
        ("Production Readiness", "Fully operational, scalable Spring Boot + React application", "Release Ready", "green")
    ],
    status_color=C_GREEN,
    extra_note="• Verified end-to-end on live MySQL 8 and Spring Boot 3 environment"
)

print("All 20 mockup graphics generated successfully!")

# Now build the PowerPoint presentation
prs = Presentation()
prs.slide_width = Inches(13.333)
prs.slide_height = Inches(7.5)
blank_slide_layout = prs.slide_layouts[6]

slides_data = [
    # 1. Hero
    {
        "slide_num": 1,
        "category": "PROJECT PRESENTATION",
        "title": "KLMedia Social Platform",
        "subtitle": "Enterprise-Grade Full-Stack Social Network with JWT Security & Email Verification",
        "bullets": [
            "Modern, reactive social media experience designed with React 18 and Spring Boot 3.",
            "Complete authentication lifecycle: Registration, 6-digit OTP email verification, and JWT login.",
            "Enterprise security architecture with BCrypt password hashing and role-based access control.",
            "Rich social ecosystem: Home feed, post creation, threaded comments, atomic likes, and search.",
            "Designed and styled in a dark ocean aesthetic with glassmorphism components."
        ],
        "image": "img_01_hero.png",
        "tech_badge": "React 18  •  Spring Boot 3  •  MySQL 8  •  JWT  •  JavaMailSender"
    },
    # 2. Architecture
    {
        "slide_num": 2,
        "category": "SYSTEM ARCHITECTURE",
        "title": "Full-Stack System Blueprint",
        "subtitle": "Modern Multi-Tier Architecture for High Concurrency & Low Latency",
        "bullets": [
            "Client Tier: React 18 SPA bundled with Vite, Axios Bearer interceptor, and Lucide UI icons.",
            "Security Gateway: JwtAuthFilter intercepts incoming requests to enforce stateless authentication.",
            "Service Tier: Loosely coupled Spring @Service layer handling business logic and rate limiting.",
            "Persistence Tier: Spring Data JPA ORM with Hibernate managing MySQL relational tables.",
            "Communication Engine: JavaMailSender SMTP integration for responsive HTML notifications."
        ],
        "image": "img_02_arch.png",
        "tech_badge": "RESTful Micro-Services  •  Stateless Security  •  JPA Hibernate"
    },
    # 3. Security Framework
    {
        "slide_num": 3,
        "category": "SECURITY ARCHITECTURE",
        "title": "Zero-Trust Security & JWT Gateway",
        "subtitle": "Robust Defense-in-Depth Protecting Endpoints, Data & User Sessions",
        "bullets": [
            "Stateless JWT Authentication: Generates HMAC-SHA384 signed tokens with 24-hour expiration.",
            "Salted BCrypt Encryption: Passwords hashed with standard 10 salt rounds before persistence.",
            "Hashed Verification Codes: OTP codes are never stored in plain text—only BCrypt hashes in MySQL.",
            "Cooldown Rate Limiter: Enforces a 60-second cooldown per email to prevent spam and DoS attacks.",
            "Scoped Authorization: Pre-configured Spring Security rules separating USER and ADMIN privileges."
        ],
        "image": "img_03_security.png",
        "tech_badge": "HMAC-SHA384  •  BCrypt  •  Anti-Brute Force  •  CORS Whitelisting"
    },
    # 4. Sign In Page
    {
        "slide_num": 4,
        "category": "AUTHENTICATION MODULE",
        "title": "Sign In Page & Role-Based Entry",
        "subtitle": "Dual-Mode Authentication Portal with Instant Role Switching",
        "bullets": [
            "Dual Role Selector: Interactive tab toggle between standard User Login and Admin Login.",
            "Spring Security Credential Check: Validates credentials via AuthService and PasswordEncoder.",
            "Show / Hide Password: Password visibility toggles with reactive Lucide Eye and EyeOff icons.",
            "Unverified Account Interceptor: Prompts unverified users with a direct 'Verify Email Now' shortcut.",
            "Token Management: Stores access token in localStorage and attaches it to subsequent API headers."
        ],
        "image": "img_04_signin.png",
        "tech_badge": "POST /api/auth/login  •  POST /api/auth/admin-login  •  Bearer JWT"
    },
    # 5. Sign Up Page
    {
        "slide_num": 5,
        "category": "USER ONBOARDING",
        "title": "Registration (Sign Up) Architecture",
        "subtitle": "User Onboarding Pipeline with Comprehensive Input Validation",
        "bullets": [
            "Registration Form: Username, Email address, Password, and Confirm Password fields.",
            "Client & Server Validation: Email regex check, 6+ character password strength, and password matching.",
            "Collision Detection: Validates that email and username do not already exist among active accounts.",
            "Pending Verification Status: Initializes account with emailVerified=false in MySQL database.",
            "Automatic OTP Dispatch: Generates and hashes a 6-digit verification code, dispatching it via SMTP."
        ],
        "image": "img_05_signup.png",
        "tech_badge": "POST /api/auth/register  •  @Valid RegisterRequest  •  BCrypt"
    },
    # 6. Email Verification
    {
        "slide_num": 6,
        "category": "IDENTITY VERIFICATION",
        "title": "Email Verification & OTP System",
        "subtitle": "Secure 6-Digit Email Activation with SMTP and Cooldown Timers",
        "bullets": [
            "Cryptographic OTP Generation: Uses SecureRandom to issue unique 6-digit codes (100000–999999).",
            "5-Minute Expiration: Enforces strict 5-minute time-to-live before code invalidation in database.",
            "Branded HTML Templates: Dispatches styled emails featuring KLU branding, codes, and security tips.",
            "Anti-Abuse Cooldown: 60-second reactive countdown timer prevents rapid resend requests.",
            "One-Time Activation: Clears verificationCodeHash upon successful match and logs the user in."
        ],
        "image": "img_06_verify.png",
        "tech_badge": "POST /api/auth/verify-email  •  POST /api/auth/resend-verification"
    },
    # 7. Forgot Password
    {
        "slide_num": 7,
        "category": "ACCOUNT RECOVERY",
        "title": "Forgot Password Workflow",
        "subtitle": "Privacy-First Account Recovery Preventing User Enumeration",
        "bullets": [
            "Email-Based Recovery: Registered users enter email address to request a password reset.",
            "Zero Enumeration Disclosure: Returns a generic success notice regardless of email existence.",
            "Secure OTP Storage: Generates a 6-digit reset code hashed with BCrypt (resetCodeHash).",
            "Time-Limited Session: Code expires in 5 minutes; rate-limited with 60-second cooldown.",
            "Seamless Navigation: Directs user to Step 2 (Reset Password Screen) with email preserved."
        ],
        "image": "img_07_forgot.png",
        "tech_badge": "POST /api/auth/forgot-password  •  Privacy Preserved  •  5-Min TTL"
    },
    # 8. Reset Password
    {
        "slide_num": 8,
        "category": "CREDENTIAL MANAGEMENT",
        "title": "Password Reset & Invalidation",
        "subtitle": "Secure Multi-Step Password Overwrite with Immediate Token Invalidation",
        "bullets": [
            "Code Verification: POST /api/auth/verify-reset-code validates OTP code match before password input.",
            "Password Confirmation: Prompts for New Password and Confirm Password with eye visibility toggles.",
            "BCrypt Salting & Storage: Encrypts new password with BCrypt before saving to MySQL users table.",
            "One-Time Replay Guard: Invalidates resetCodeHash and resetCodeExpiry immediately after update.",
            "Feedback & Auto-Redirect: Displays clear success alert and redirects user to Login after 2 seconds."
        ],
        "image": "img_08_reset.png",
        "tech_badge": "POST /api/auth/reset-password  •  One-Time Use  •  BCrypt Hashed"
    },
    # 9. Admin Dashboard
    {
        "slide_num": 9,
        "category": "GOVERNANCE & AUDIT",
        "title": "Admin Dashboard & System Metrics",
        "subtitle": "Centralized Administration Console for Platform Governance",
        "bullets": [
            "Protected Route: Restricted to authenticated users with ADMIN role via Spring Security.",
            "Real-Time Statistics: Displays live metric cards: Total Registered Users, Normal Users, Admins.",
            "Community Overview: Audits all registered accounts with ID, Username, Email, and Assigned Role.",
            "Dedicated Admin Header: Customized navigation bar with administrative indicators and quick logout.",
            "Modern Glassmorphism UI: Dark ocean cards designed for high legibility and operational speed."
        ],
        "image": "img_09_admin.png",
        "tech_badge": "GET /api/admin/users  •  hasRole('ADMIN')  •  Real-Time Metrics"
    },
    # 10. User Add & Delete Role
    {
        "slide_num": 10,
        "category": "ADMIN OPERATIONS",
        "title": "User Management: Add & Delete Operations",
        "subtitle": "Administrative User Management with Role-Based Protection",
        "bullets": [
            "Direct User Provisioning: Admin can manually create new community users directly from dashboard.",
            "Input Sanitization: Validates unique username, email format, and password criteria before insert.",
            "Safe Deletion Pipeline: DELETE /api/admin/users/{id} removes inactive or violating user accounts.",
            "Root Admin Protection: Enforces strict check preventing accidental deletion of admin accounts.",
            "Optimistic Table Sync: Automatically re-fetches and updates the user table upon mutation."
        ],
        "image": "img_10_user_crud.png",
        "tech_badge": "POST /api/admin/users  •  DELETE /api/admin/users/{id}  •  Protected Root"
    },
    # 11. User Home Feed
    {
        "slide_num": 11,
        "category": "CORE SOCIAL EXPERIENCE",
        "title": "User Home Feed & Content Stream",
        "subtitle": "Chronological Community Activity Stream with Glassmorphism Cards",
        "bullets": [
            "Dynamic Feed Aggregation: Queries posts ordered by createdAt DESC across all community members.",
            "Rich Post Headers: Displays author username, circular initial avatar, and formatted timestamp.",
            "Responsive Media Display: Handles embedded images with dark placeholder frames and rounded borders.",
            "Owner Controls: Dynamically displays Trash icon allowing creators to delete their own posts.",
            "Fluid Layout: Single-column feed design with smooth scrolling and responsive mobile behavior."
        ],
        "image": "img_11_feed.png",
        "tech_badge": "GET /api/posts  •  React State Synchronization  •  Responsive Cards"
    },
    # 12. Create Post Page
    {
        "slide_num": 12,
        "category": "CONTENT CREATION",
        "title": "Create Post & Media Publishing",
        "subtitle": "Intuitive Content Composer with Instant Feed Propagation",
        "bullets": [
            "Multi-Line Text Composer: Auto-expanding textarea for drafting captions and status updates.",
            "Media URL Embedding: Support for image URLs with preview rendering directly inside the feed.",
            "Client Feedback States: Displays loading indicators and disabled states during submission.",
            "Optimistic Prepend: Newly published post appears immediately at the top of the user feed.",
            "Ownership Flagging: Backend automatically tags post with ownedByMe=true for the current user."
        ],
        "image": "img_12_create.png",
        "tech_badge": "POST /api/posts  •  PostRequest DTO  •  PostService Pipeline"
    },
    # 13. Engagement: Likes & Comments
    {
        "slide_num": 13,
        "category": "INTERACTION ENGINE",
        "title": "Engagement Engine: Likes & Comments",
        "subtitle": "Atomic Interaction Pipeline with Real-Time Feedback",
        "bullets": [
            "Atomic Like Toggle: POST /api/posts/{id}/like toggles like state in post_likes table.",
            "Visual Heart State: Heart icon illuminates in vibrant cyan with animated like count update.",
            "Threaded Discussion: GET /api/posts/{id}/comments loads post commentary on demand.",
            "Comment Posting: POST /api/posts/{id}/comments allows instant comment submissions.",
            "Comment Metadata: Formatted author username, avatar, and relative timestamp per comment."
        ],
        "image": "img_13_engage.png",
        "tech_badge": "POST /api/posts/{id}/like  •  POST /api/posts/{id}/comments"
    },
    # 14. Search Page
    {
        "slide_num": 14,
        "category": "USER DISCOVERY",
        "title": "User Discovery & Search Engine",
        "subtitle": "Live Community Search with Debounced Querying",
        "bullets": [
            "Live Query Search: Search bar with real-time reactive input and debounce optimization.",
            "Substring Database Query: Searches usernames containing search query text in MySQL users table.",
            "Self-Exclusion Guard: Automatically omits current logged-in user from search results.",
            "Rich Result Cards: Displays user avatar, username, bio preview, and total posts published.",
            "Quick Profile Navigation: Clicking any user card directs instantly to their public profile."
        ],
        "image": "img_14_search.png",
        "tech_badge": "GET /api/users/search?q=...  •  Limit 20 Results  •  Debounced Search"
    },
    # 15. User Profile Page
    {
        "slide_num": 15,
        "category": "USER SHOWCASE",
        "title": "Personal & Public Profile Pages",
        "subtitle": "User Bio, Social Metrics & Three-Column Post Gallery",
        "bullets": [
            "Private Profile (/profile): Displays personal account data, post counts, and bio overview.",
            "Public Profiles (/profile/:id): View other community members with full social metrics.",
            "Activity Counters: Live counters showing Posts count, Followers count, and Following count.",
            "Follow State Indicator: Shows whether current user follows this profile with interactive CTA.",
            "Post Gallery Grid: Responsive 3-column photo grid displaying all posts created by the user."
        ],
        "image": "img_15_profile.png",
        "tech_badge": "GET /api/users/me  •  GET /api/users/{id}  •  Social Metrics"
    },
    # 16. Followers & Following
    {
        "slide_num": 16,
        "category": "SOCIAL NETWORK",
        "title": "Social Graph: Followers & Following",
        "subtitle": "Relational Social Network Graph with Live Connection Tracking",
        "bullets": [
            "Asymmetric Social Graph: follows table maps follower_id to followed_id relationships.",
            "Follow & Unfollow CTA: POST /api/follow/{id} and DELETE /api/follow/{id} toggles connection.",
            "Stateful Follow Verification: FollowService.isFollowing(userEmail, targetUserId) checks state.",
            "Synchronized Counts: Automatically computes followersCount and followingCount via JPA.",
            "Live Notification Trigger: Initiating a follow triggers an unread notification to the recipient."
        ],
        "image": "img_16_follow.png",
        "tech_badge": "POST /api/follow/{id}  •  DELETE /api/follow/{id}  •  follows Table"
    },
    # 17. Notification Page
    {
        "slide_num": 17,
        "category": "COMMUNICATION",
        "title": "Real-Time Notification Center",
        "subtitle": "Unified Activity Feed with Dynamic Badge Counter",
        "bullets": [
            "Multi-Event Alerts: Centralized notifications for Likes, Comments, and New Follower events.",
            "Sidebar Unread Badge: Global notification badge reflects live unread notifications counter.",
            "Unread Highlighting: Unread alerts visually highlighted with glowing cyan borders and badges.",
            "Mark as Read: PATCH /api/notifications/mark-read updates read status in database.",
            "Notification Pruning: DELETE /api/notifications/{id} allows users to dismiss alerts individually."
        ],
        "image": "img_17_notifications.png",
        "tech_badge": "GET /api/notifications  •  GET /api/notifications/unread-count"
    },
    # 18. Messenger & Direct Chat
    {
        "slide_num": 18,
        "category": "DIRECT MESSAGING",
        "title": "Messenger & Peer-to-Peer Chat",
        "subtitle": "Direct Messaging Hub for Verified Community Members",
        "bullets": [
            "Conversation Stream: Sidebar listing all active dialogues with last-message snippet preview.",
            "Direct User Chat: Select any verified community member to start a one-on-one conversation.",
            "Chronological Message Log: Distinct speech bubbles differentiating incoming vs outgoing messages.",
            "Direct Message Composer: Smooth input box with enter-to-send support and action buttons.",
            "Real-Time Readiness: Built on structured messages table ready for WebSocket scaling."
        ],
        "image": "img_18_messenger.png",
        "tech_badge": "POST /api/messages  •  GET /api/messages/{id}  •  messages Table"
    },
    # 19. Database Schema & Architecture
    {
        "slide_num": 19,
        "category": "DATABASE ARCHITECTURE",
        "title": "MySQL Relational Schema & Models",
        "subtitle": "Normalized Entity-Relationship Design with Referential Integrity",
        "bullets": [
            "users: Core table storing credentials, roles, email_verified, and verification/reset hashes.",
            "posts: Stores post captions, image URLs, creation timestamps, and user_id foreign keys.",
            "post_comments & post_likes: Relational interaction records with composite unique indices.",
            "follows: Social graph mapping follower_id and followed_id foreign keys with JPA joins.",
            "notifications & messages: Transactional tables managing user communications and alert states."
        ],
        "image": "img_19_db.png",
        "tech_badge": "MySQL 8.0  •  Spring Data JPA  •  Hibernate DDL  •  Foreign Keys"
    },
    # 20. Testing & Verification
    {
        "slide_num": 20,
        "category": "QUALITY ASSURANCE",
        "title": "Testing Matrix & Production Roadmap",
        "subtitle": "End-to-End Verification Across All 12 Functional Requirements",
        "bullets": [
            "Authentication Testing: Registration, 6-digit OTP verification, and JWT login verified 100%.",
            "Recovery Testing: Non-enumerating forgot password, OTP delivery, and BCrypt reset verified.",
            "Access Control: USER vs ADMIN isolation and admin user creation/deletion tested successfully.",
            "Social Feature Testing: Posts, likes, comments, follows, search, and notifications verified.",
            "Future Roadmap: WebSocket live chat, S3 media uploads, and React Native mobile client."
        ],
        "image": "img_20_test.png",
        "tech_badge": "12/12 Tests Passed  •  Zero Regression  •  Production Ready"
    }
]

print("Assembling PowerPoint presentation...")

for slide_idx, sdata in enumerate(slides_data):
    slide = prs.slides.add_slide(blank_slide_layout)

    # 1. Background fill
    bg = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, Inches(13.333), Inches(7.5))
    bg.fill.solid()
    bg.fill.fore_color.rgb = PPT_DARK_BG
    bg.line.fill.background()

    # 2. Header Bar with Logo
    header_box = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(0.4), Inches(11.733), Inches(1.1))
    header_box.fill.solid()
    header_box.fill.fore_color.rgb = PPT_CARD_BG
    header_box.line.color.rgb = PPT_BORDER
    header_box.line.width = Pt(1)

    # Logo in Header
    if os.path.exists(LOGO_PATH):
        slide.shapes.add_picture(LOGO_PATH, Inches(0.95), Inches(0.48), width=Inches(0.94), height=Inches(0.94))

    # Header Titles (Breadcrumb Category & Main Title)
    tf_h = header_box.text_frame
    tf_h.vertical_anchor = MSO_ANCHOR.MIDDLE
    tf_h.word_wrap = True
    p0 = tf_h.paragraphs[0]
    p0.text = f"{sdata['category']}  •  SLIDE {sdata['slide_num']}/20"
    p0.font.size = Pt(11)
    p0.font.bold = True
    p0.font.color.rgb = PPT_CYAN
    p0.alignment = PP_ALIGN.LEFT

    p1 = tf_h.add_paragraph()
    p1.text = sdata['title']
    p1.font.size = Pt(22)
    p1.font.bold = True
    p1.font.color.rgb = PPT_WHITE
    p1.alignment = PP_ALIGN.LEFT

    # Adjust text frame left margin so it doesn't overlap logo
    tf_h.margin_left = Inches(1.25)
    tf_h.margin_top = Inches(0.12)

    # 3. Left Content Card (Text, Subtitle, Bullets)
    left_card = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(1.7), Inches(6.0), Inches(5.0))
    left_card.fill.solid()
    left_card.fill.fore_color.rgb = PPT_CARD_BG
    left_card.line.color.rgb = PPT_BORDER
    left_card.line.width = Pt(1)

    tf_l = left_card.text_frame
    tf_l.word_wrap = True
    tf_l.margin_left = Inches(0.35)
    tf_l.margin_right = Inches(0.35)
    tf_l.margin_top = Inches(0.3)
    tf_l.margin_bottom = Inches(0.25)

    # Subtitle
    p_sub = tf_l.paragraphs[0]
    p_sub.text = sdata['subtitle']
    p_sub.font.size = Pt(14)
    p_sub.font.bold = True
    p_sub.font.color.rgb = PPT_CYAN_LIGHT
    p_sub.space_after = Pt(14)

    # Bullets
    for b_idx, bullet in enumerate(sdata['bullets']):
        pb = tf_l.add_paragraph()
        pb.text = f"•  {bullet}"
        pb.font.size = Pt(12.5)
        pb.font.color.rgb = PPT_WHITE if b_idx == 0 else PPT_GRAY
        pb.space_after = Pt(9)
        pb.line_spacing = 1.15

    # Tech badge at bottom of left card
    p_badge = tf_l.add_paragraph()
    p_badge.text = f"⚙️  {sdata['tech_badge']}"
    p_badge.font.size = Pt(11)
    p_badge.font.bold = True
    p_badge.font.color.rgb = PPT_CYAN
    p_badge.space_before = Pt(8)

    # 4. Right Visual Graphic Card
    img_filename = sdata['image']
    img_filepath = os.path.join(IMG_DIR, img_filename)
    if os.path.exists(img_filepath):
        # Insert image
        slide.shapes.add_picture(img_filepath, Inches(7.0), Inches(1.7), width=Inches(5.533), height=Inches(5.0))

    # 5. Footer Line & Caption
    footer = slide.shapes.add_textbox(Inches(0.8), Inches(6.85), Inches(11.733), Inches(0.45))
    tf_f = footer.text_frame
    p_f = tf_f.paragraphs[0]
    p_f.text = "KLMedia Enterprise Social Platform  |  React.js • Spring Boot • MySQL • JWT • JavaMailSender  |  Confidential & Proprietary"
    p_f.font.size = Pt(9.5)
    p_f.font.color.rgb = PPT_GRAY
    p_f.alignment = PP_ALIGN.CENTER

# Save file
out_pptx_1 = os.path.join(BASE_DIR, "KLMedia_Project_Presentation.pptx")
out_pptx_2 = os.path.join("C:\\Users\\Dell\\Desktop", "KLMedia_Project_Presentation.pptx")

prs.save(out_pptx_1)
try:
    shutil.copy2(out_pptx_1, out_pptx_2)
    print(f"Presentation copied to Desktop: {out_pptx_2}")
except Exception as e:
    print(f"Desktop copy warning: {e}")

print(f"Presentation generated successfully with 20 slides at: {out_pptx_1}")

