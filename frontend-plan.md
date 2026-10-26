# Order Processing Service — Frontend Plan

**Stack**: React 18 + Vite | Vanilla CSS | Axios | React Router v6  
**Backend**: `http://localhost:8080/api/v1`  
**Date**: 26 October 2026

---

## 1. Design Philosophy

### Inspired by Flipkart & Amazon — but different

| What they do well | What we borrow | What we do differently |
|---|---|---|
| Data-dense tables | Compact rows, no wasted whitespace | **Two-pane layout** — list + detail side by side (Gmail style) |
| Status badges | Color-coded status pills | **Vertical timeline tracker** instead of just a badge |
| Order cards | Clear product line items | **Live detail panel** — clicking a row updates the right pane without navigation |
| Top navbar | Global nav + user menu | **Role-aware nav** — ADMIN and CUSTOMER see different items |
| Action buttons | Clear, purposeful CTAs | **Inline drawer** for Create Order — no full page navigation |

### Design Principles
- **No gradients, no glassmorphism** — flat surfaces with subtle border shadows
- **Data first** — information is always visible, not hidden behind modals
- **Color means something** — status colors are consistent everywhere (badge, row, timeline)
- **Compact but breathable** — 14px base font, 4px grid, enough padding to read

### Color System

```
--color-surface:        #F7F8FA   /* off-white page background */
--color-card:           #FFFFFF   /* card/panel background */
--color-border:         #E2E5EA   /* subtle borders */
--color-text-primary:   #1C2333   /* headings */
--color-text-secondary: #5B6477   /* labels, metadata */
--color-text-muted:     #9BA5B4   /* placeholders, hints */

--color-brand:          #2563EB   /* primary CTA blue (professional, not Amazon orange) */
--color-brand-hover:    #1D4ED8

--status-pending:       #F59E0B   /* amber */
--status-confirmed:     #3B82F6   /* blue */
--status-shipped:       #8B5CF6   /* purple */
--status-delivered:     #10B981   /* green */
--status-cancelled:     #EF4444   /* red */

--color-danger:         #EF4444
--color-success:        #10B981
```

### Typography
- Font: **Inter** (Google Fonts) — clean, professional, legible in tables
- Base: 14px / 1.5 line-height
- Headings: 600 weight, no uppercase tricks

---

## 2. Pages & Routes

| Route | Component | Roles | Description |
|---|---|---|---|
| `/login` | `LoginPage` | Public | Email + password form |
| `/register` | `RegisterPage` | Public | Registration form |
| `/dashboard` | `DashboardPage` | ADMIN | Stats + orders table + filters |
| `/orders` | `OrdersPage` | CUSTOMER | Personal orders, two-pane layout |
| `/orders/:id` | `OrderDetailPage` | Both | Full order detail + timeline |
| `*` | `NotFoundPage` | — | 404 redirect |

---

## 3. Layout Design

### ADMIN — Dashboard Layout

```
┌─────────────────────────────────────────────────────────────────┐
│  NAVBAR: Logo | Orders | Export CSV | [admin@mail.com ▾]        │
├─────────────────────────────────────────────────────────────────┤
│  STAT CARDS:  [Total Orders]  [Pending]  [Revenue]  [Today]     │
├──────────────────────────────────┬──────────────────────────────┤
│  ORDERS TABLE                    │  ORDER DETAIL PANEL          │
│  ┌──────────────────────────┐    │  ┌────────────────────────┐  │
│  │Filter: Status | Date     │    │  │ Order #3f2a...         │  │
│  ├────┬──────────┬──────────┤    │  │ Pravin Mali            │  │
│  │ ID │ Customer │ Status ▾ │    │  │                        │  │
│  ├────┼──────────┼──────────┤    │  │ ● PENDING              │  │
│  │ …  │ Pravin   │ PENDING  │◄───┤  │ ● CONFIRMED ← current  │  │
│  │ …  │ Jane     │ SHIPPED  │    │  │ ○ SHIPPED              │  │
│  │ …  │ …        │ …        │    │  │ ○ DELIVERED            │  │
│  └────┴──────────┴──────────┘    │  │                        │  │
│                                  │  │ [Change Status ▾]      │  │
│                                  │  │ [Download Invoice PDF]  │  │
│                                  └──┴────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### CUSTOMER — My Orders Layout

```
┌─────────────────────────────────────────────────────────────────┐
│  NAVBAR: Logo | My Orders | [pravin@mail.com ▾]                 │
├─────────────────────────────────────────────────────────────────┤
│  My Orders (3)                          [+ Create New Order]    │
├──────────────────────────────────┬──────────────────────────────┤
│  ORDER LIST (left pane)          │  ORDER DETAIL (right pane)   │
│  ┌────────────────────────────┐  │  ┌────────────────────────┐  │
│  │ ● #3f2a  CONFIRMED  ₹60k  │  │  │ Order #3f2a            │  │
│  │   26 Oct 2026 · 2 items   │  │  │ ─────────────────────  │  │
│  ├────────────────────────────┤  │  │ TIMELINE               │  │
│  │ ● #8b7c  SHIPPED    ₹25k  │◄─┤  │  ✓ Order Placed        │  │
│  │   25 Oct 2026 · 1 item    │  │  │  ✓ Confirmed           │  │
│  ├────────────────────────────┤  │  │  ◎ Shipped ← here     │  │
│  │ ● #1a2d  PENDING    ₹12k  │  │  │  ○ Delivered           │  │
│  └────────────────────────────┘  │  │                        │  │
│                                  │  │ ITEMS                  │  │
│                                  │  │ Laptop  ×1  ₹50,000   │  │
│                                  │  │ Mouse   ×2  ₹1,000    │  │
│                                  │  │ ─────────────────────  │  │
│                                  │  │ Total: ₹60,180         │  │
│                                  │  │ [📄 Download Invoice]  │  │
│                                  └──┴────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Create Order — Side Drawer

```
┌────────────────────────────┐  ┌──────────────────────────────┐
│  (blurred background page) │  │  Create Order                │
│                            │  │  ─────────────────────────── │
│                            │  │  Customer Name  [          ] │
│                            │  │  Email          [          ] │
│                            │  │  Phone          [          ] │
│                            │  │  Address        [          ] │
│                            │  │  ─────────────────────────── │
│                            │  │  ITEMS                       │
│                            │  │  + Product ──── Qty ─ Price  │
│                            │  │  [Laptop    ]  [1]  [50000] │
│                            │  │  [Mouse     ]  [2]  [500  ] │
│                            │  │  [+ Add Item]                │
│                            │  │  ─────────────────────────── │
│                            │  │  Subtotal   ₹51,000          │
│                            │  │  GST 18%    ₹9,180           │
│                            │  │  Total      ₹60,180          │
│                            │  │                              │
│                            │  │  [Cancel]  [Place Order →]   │
└────────────────────────────┘  └──────────────────────────────┘
```

---

## 4. Component Architecture

```
src/
├── main.jsx
├── App.jsx                         # Router + PrivateRoute
│
├── api/
│   ├── axios.js                    # Axios instance, interceptor for JWT header
│   ├── auth.js                     # register(), login()
│   └── orders.js                   # createOrder(), getOrders(), getOrderById(),
│                                   # updateStatus(), downloadInvoice(), exportCsv()
│
├── context/
│   └── AuthContext.jsx             # JWT token, user info, login/logout
│
├── components/
│   ├── layout/
│   │   ├── Navbar.jsx
│   │   └── PrivateRoute.jsx
│   ├── orders/
│   │   ├── OrderListPanel.jsx      # Left pane — compact order rows
│   │   ├── OrderDetailPanel.jsx    # Right pane — detail + timeline + invoice
│   │   ├── OrderTimeline.jsx       # Vertical step tracker
│   │   ├── OrderItemsTable.jsx     # Line items table
│   │   └── StatusBadge.jsx        # Color-coded pill — used everywhere
│   ├── dashboard/
│   │   ├── StatCard.jsx
│   │   └── AdminOrderTable.jsx     # Full-width table with status filter
│   └── common/
│       ├── Drawer.jsx              # Slide-in panel (Create Order)
│       ├── Spinner.jsx
│       ├── EmptyState.jsx
│       └── ErrorBanner.jsx
│
├── pages/
│   ├── LoginPage.jsx
│   ├── RegisterPage.jsx
│   ├── DashboardPage.jsx           # ADMIN
│   ├── OrdersPage.jsx              # CUSTOMER two-pane
│   └── NotFoundPage.jsx
│
└── styles/
    ├── index.css                   # CSS variables, reset, typography
    ├── navbar.css
    ├── auth.css
    ├── dashboard.css
    ├── orders.css
    ├── timeline.css
    ├── drawer.css
    └── table.css
```

---

## 5. API Integration Map

| UI Action | API Call | Notes |
|---|---|---|
| Login | `POST /auth/login` | Store JWT in `localStorage`, decode role |
| Register | `POST /auth/register` | Auto-login after register |
| List orders (ADMIN) | `GET /orders?page=0&size=20&sort=createdAt,desc` | Paginated |
| List orders (CUSTOMER) | `GET /orders` | Backend filters by JWT principal |
| Order detail | `GET /orders/{id}` | Load in right pane on row click |
| Create order | `POST /orders` | From drawer, refresh list on success |
| Update status (ADMIN) | `PATCH /orders/{id}/status` | Dropdown in detail panel |
| Download invoice | `GET /orders/{id}/invoice` | `responseType: blob` → trigger download |
| Export CSV (ADMIN) | `GET /export/orders/csv` | `responseType: blob` → trigger download |

---

## 6. Execution Roadmap (Intuitive Modules)

### Module F1 — Project scaffold + design system
- Init Vite React app in `frontend/`
- `index.css` with all CSS variables, Inter font, reset
- Folder structure

### Module F2 — Auth (Login + Register pages)
- `AuthContext` (JWT storage, decode, logout)
- `LoginPage`, `RegisterPage`
- `PrivateRoute` + role-based redirect

### Module F3 — API layer
- Axios instance with JWT interceptor
- `auth.js`, `orders.js` API modules

### Module F4 — Layout + Navbar
- `Navbar` (role-aware links, user dropdown)
- Route structure in `App.jsx`

### Module F5 — Customer "My Orders" (two-pane)
- `OrdersPage` with split layout
- `OrderListPanel` (left)
- `OrderDetailPanel` + `OrderTimeline` (right)
- `StatusBadge` component

### Module F6 — Create Order Drawer
- `Drawer` component (slide-in from right)
- Multi-item form with live total preview
- `POST /orders` integration

### Module F7 — Admin Dashboard
- `DashboardPage` with stat cards
- `AdminOrderTable` with status filter dropdown
- Status change dropdown in detail panel
- CSV export button

### Module F8 — Invoice download + polish
- PDF download integration (`responseType: blob`)
- Loading states, error banners
- Empty states
- Responsive adjustments

---

## 7. Key UI Decisions

| Decision | Rationale |
|---|---|
| Two-pane layout | Keeps context — user sees both list and detail without navigating away |
| Vertical timeline tracker | More intuitive than just a status badge for a sequential flow |
| Slide-in drawer for create | Doesn't lose current context (which order is selected in the list) |
| Stat cards only for ADMIN | CUSTOMER has no need for aggregate stats — show only personal data |
| Role-aware nav | CUSTOMER never sees admin-only routes even in the URL bar |
| Live total in drawer | Immediate feedback on price as items are added — Amazon-inspired |
| Flat design, no gradients | Professional SaaS look, not a consumer shopping app |
