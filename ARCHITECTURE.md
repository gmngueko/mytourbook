# MyTourbook Project Architecture

## System Overview

MyTourbook is a desktop application built on **Eclipse RCP (Rich Client Platform)** using Java for recording, organizing, and visualizing travel tours and trips. The application integrates with GPS devices, cameras, and map services to provide a comprehensive tour management experience.

---

## Architecture Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                       MyTourbook Desktop App                     │
│                    (Eclipse RCP-based - Java)                    │
└─────────────────────────────────────────────────────────────────┘
                               │
                ┌──────────────┴──────────────┐
                │                             │
       ┌────────▼─────────┐         ┌────────▼──────────┐
       │   UI/View Layer  │         │  Data/Model Layer │
       └──────────────────┘         └───────────────────┘
               │                             │
       ┌───────┴────────┐          ┌────────┴────────┐
       │                │          │                 │
   ┌───▼──────┐  ┌─────▼──┐  ┌───▼─────┐    ┌─────▼────┐
   │  Common  │  │ Chart  │  │ Model   │    │Database  │
   │  (UI)    │  │ Charts │  │(Tourism)│    │ (Tour DB)│
   └──────────┘  └────────┘  └─────────┘    └──────────┘
       │              │
   ┌───┴─────┐    ┌───┴─────┐
   │ Branding│    │Statistics
   └─────────┘    └──────────┘
       │                │
   ┌───▼────────────────▼────┐
   │   Device Integration     │
   └─────────────────────────┘
       │
   ┌───┴─────────────────────────┐
   │                             │
   ├─ Garmin (FIT format)        │
   ├─ NMEA (GPS devices)         │
   ├─ RXTX (Serial port - OS)    │
   └─────────────────────────────┘
       │
   ┌───▼───────────────┐
   │ Import/Export     │
   │ Processing        │
   └───────────────────┘
       │
   ┌───▼──────────────┐
   │ Additional Tools │
   ├─ Photo Module    │
   ├─ Printing        │
   ├─ Web Interface   │
   ├─ Cloud Sync      │
   ├─ Map Rendering   │
   │  (WorldWind 3D)  │
   │  (VTM Map)       │
   └───────────────────┘
```

---

## Core Components

### **1. UI/View Layer**
- **net.tourbook** - Main application entry point and orchestrator
- **net.tourbook.common** - Shared UI utilities and helpers
- **net.tourbook.branding** - Application branding and theming

### **2. Data Visualization**
- **net.tourbook.chart** - Chart and graph visualization for tour statistics
- **net.tourbook.statistics** - Tour analytics and performance metrics

### **3. Data Model & Persistence**
- **net.tourbook.model** - Core data entities (Tour, Track, Waypoint, etc.)
- **net.tourbook.ext.ejb3_3.4.0** - Hibernate ORM framework for database persistence

### **4. Device Integration Layer**
#### Base Layer:
- **net.tourbook.device** - Abstract device interface and base implementations

#### Device Adapters:
- **net.tourbook.device.garmin** - Garmin device support
- **net.tourbook.device.garmin.fit** - Garmin FIT file format parser
- **net.tourbook.device.nmea** - NMEA protocol support for GPS devices

#### Serial Communication (Platform-specific):
- **net.tourbook.ext.rxtx_2.2** - Base RXTX library
- **net.tourbook.ext.rxtx.win64_2.2** - Windows 64-bit native binaries
- **net.tourbook.ext.rxtx.linux64_2.2** - Linux 64-bit native binaries
- **net.tourbook.ext.rxtx.macosx_2.2** - macOS native binaries

### **5. Data Processing**
- **net.tourbook.export** - Tour data export functionality
- **net.tourbook.photo** - Photo integration and management with tours

### **6. Output & Reporting**
- **net.tourbook.printing** - Tour report printing functionality
- **net.tourbook.web** - Web interface and remote access capabilities
- **net.tourbook.cloud** - Cloud synchronization and backup services

### **7. Map Rendering & Visualization**

#### NASA WorldWind 3D Globe:
- **net.tourbook.ext.worldwind** - Core WorldWind integration
- **net.tourbook.ext.worldwind.win64** - Windows 64-bit support
- **net.tourbook.ext.worldwind.linux64** - Linux 64-bit support
- **net.tourbook.ext.worldwind.macosx** - macOS support

#### Vector Tile Maps (VTM):
- **net.tourbook.ext.vtm** - Core VTM library
- **net.tourbook.ext.vtm.windows** - Windows platform binaries
- **net.tourbook.ext.vtm.linux** - Linux platform binaries
- **net.tourbook.ext.vtm.macosx** - macOS platform binaries

### **8. External Libraries & Dependencies**
- **net.tourbook.ext.apache** - Apache Commons utilities
- **net.tourbook.ext.velocity** - Template engine for reports
- **net.tourbook.ext.geoclipse** - GIS utilities and geographic functions
- **net.tourbook.ext.jars** - General utility JAR dependencies
- **net.tourbook.ext.jars.printing** - Printing-related dependencies

### **9. Build & Plugin System**
- **net.tourbook.p2** - Eclipse P2 provisioning and plugin management

---

## Testing Infrastructure

- **net.tourbook.tests** - Unit tests for core business logic
- **net.tourbook.ui.tests** - UI integration tests using SWTBot framework

---

## Technology Stack

| Layer | Technology |
|-------|-----------|
| **UI Framework** | Eclipse RCP (SWT) |
| **Language** | Java (69.2%) |
| **Data Processing** | Standard ML (28.4%) |
| **Web Components** | JavaScript (0.8%), PHP (0.6%) |
| **Build System** | Maven + Tycho |
| **ORM** | Hibernate 3.3.2 |
| **Map Rendering** | NASA WorldWind, VTM |
| **Testing** | JUnit, SWTBot |
| **Supported Protocols** | NMEA, Garmin FIT, RXTX Serial |

---

## Data Flow

```
1. User connects GPS device or imports file
   ↓
2. Device Driver (Garmin/NMEA) parses data
   ↓
3. Data Import/Export module processes records
   ↓
4. Tour Model stores data in local database
   ↓
5. UI displays tour on charts and maps
   ↓
6. User can export, print, or sync to cloud
```

---

## Platform Support

- **Windows** (64-bit with native RXTX, WorldWind, VTM support)
- **Linux** (64-bit with native RXTX, WorldWind, VTM support)
- **macOS** (with native RXTX, WorldWind, VTM support)

---

## Build Process

The project uses **Maven/Tycho** for modular builds:
- Automated compilation and packaging of Eclipse RCP bundles
- Platform-specific native binary inclusion
- P2 plugin repository generation for distribution

---

## Key Features

✅ Import GPS data from Garmin devices and FIT files  
✅ Parse NMEA protocol from various GPS devices  
✅ Visualize tours on 3D maps (WorldWind) and 2D maps (VTM)  
✅ Generate tour statistics and analytics  
✅ Integrate and organize photos with tour metadata  
✅ Print tour reports and itineraries  
✅ Export tour data in multiple formats  
✅ Sync tours to cloud storage  
✅ Web interface for remote access  

---

## License

GNU General Public License v2.0 (GPL v2)