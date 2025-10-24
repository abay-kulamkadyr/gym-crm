### Architecture Diagram
```bash
┌─────────────────────────────────────────────────────────────────────┐
│                       INTERFACE ADAPTERS                            │
│ ┌─────────────────────────────────────────────────────────────────┐ │
│ │ com.epam.interface_adapters.facade                              │ │
│ │   └── GymFacade                                                 │ │
│ │       • Coordinates application services                        │ │
│ │       • Provides unified API for external access                │ │
│ └─────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        APPLICATION LAYER                            │
│ ┌─────────────────────────────────────────────────────────────────┐ │
│ │ com.epam.application.service                                    │ │
│ │   ├── CrudService<T> (interface)                                │ │
│ │   ├── TraineeService                                            │ │
│ │   ├── TrainerService                                            │ │
│ │   ├── TrainingService                                           │ │
│ │   └── TrainingTypeService                                       │ │
│ │                                                                 │ │
│ │   • Business workflows & use cases                              │ │
│ │   • Orchestrates domain objects                                 │ │
│ └─────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          DOMAIN LAYER (CORE)                        │
│ ┌─────────────────────────────────────────────────────────────────┐ │
│ │ com.epam.domain.model                                           │ │
│ │   ├── User                                                      │ │
│ │   ├── Trainee                                                   │ │
│ │   ├── Trainer                                                   │ │
│ │   ├── Training                                                  │ │
│ │   └── TrainingType                                              │ │
│ │                                                                 │ │
│ │   • Pure business entities                                      │ │
│ │   • Domain logic & validation                                   │ │
│ └─────────────────────────────────────────────────────────────────┘ │
│                                                                     │
│ ┌─────────────────────────────────────────────────────────────────┐ │
│ │ com.epam.domain.repository (INTERFACES - Ports)                 │ │
│ │   ├── CrudRepository<T>                                         │ │
│ │   ├── TraineeRepository                                         │ │
│ │   ├── TrainerRepository                                         │ │
│ │   ├── TrainingRepository                                        │ │
│ │   └── TrainingTypeRepository                                    │ │
│ │                                                                 │ │
│ │   • Define persistence contracts                                │ │
│ └─────────────────────────────────────────────────────────────────┘ │
│                                                                     │
│ ┌─────────────────────────────────────────────────────────────────┐ │
│ │ com.epam.domain.util                                            │ │
│ │   └── PasswordGenerator                                         │ │
│ └─────────────────────────────────────────────────────────────────┘ │
│                                                                     │
│ ┌─────────────────────────────────────────────────────────────────┐ │
│ │ com.epam.domain.validation                                      │ │
│ │   └── RelationshipValidator                                     │ │
│ │       • Domain integrity checks                                 │ │
│ │       • Uses repository interfaces                              │ │
│ └─────────────────────────────────────────────────────────────────┘ │
│                                                                     │
│                     NO OUTWARD DEPENDENCIES                         │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      INFRASTRUCTURE LAYER                           │
│ ┌─────────────────────────────────────────────────────────────────┐ │
│ │ com.epam.infrastructure.dao                                     │ │
│ │   ├── TraineeRepositoryImpl                                     │ │
│ │   ├── TrainerRepositoryImpl                                     │ │
│ │   ├── TrainingRepositoryImpl                                    │ │
│ │   └── TrainingTypeRepositoryImpl                                │ │
│ │                                                                 │ │
│ │   • Implements domain repository interfaces                     │ │
│ └─────────────────────────────────────────────────────────────────┘ │
│                                                                     │
│ ┌─────────────────────────────────────────────────────────────────┐ │
│ │ com.epam.infrastructure.config                                  │ │
│ │   └── AppConfig                                                 │ │
│ │       • Spring configuration                                    │ │
│ │       • Bean definitions                                        │ │
│ │       • Storage beans with @InitializableStorage                │ │
│ └─────────────────────────────────────────────────────────────────┘ │
│                                                                     │
│ ┌─────────────────────────────────────────────────────────────────┐ │
│ │ com.epam.infrastructure.bootstrap                               │ │
│ │   ├── @InitializableStorage (annotation)                        │ │
│ │   ├── StorageInitializer<T> (interface)                         │ │
│ │   ├── StorageInitializationBeanPostProcessor                    │ │
│ │   ├── DataLoader                                                │ │
│ │   ├── TraineeStorageInitializer                                 │ │
│ │   ├── TrainerStorageInitializer                                 │ │
│ │   ├── TrainingStorageInitializer                                │ │
│ │   └── TrainingTypeStorageInitializer                            │ │
│ │                                                                 │ │
│ │   • Initializes storage from JSON                               │ │
│ │   • Spring lifecycle management                                 │ │
│ └─────────────────────────────────────────────────────────────────┘ │
│                                                                     │
│            • Depends on everything (domain, application)            │
│            • Contains framework-specific code (Spring)              │
│            • Replaceable without affecting business logic           │
└─────────────────────────────────────────────────────────────────────┘
```