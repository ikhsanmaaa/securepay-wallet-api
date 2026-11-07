package com.ikhsan.securepaywallet.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import org.junit.jupiter.api.DisplayName;

import com.ikhsan.securepaywallet.auth.security.JwtAuthenticationFilter;
import com.ikhsan.securepaywallet.auth.service.AuthService;
import com.ikhsan.securepaywallet.auth.session.annotation.SessionActivity;
import com.ikhsan.securepaywallet.auth.session.interceptor.SessionActivityInterceptor;
import com.ikhsan.securepaywallet.auth.session.service.SessionService;
import com.ikhsan.securepaywallet.common.config.SecurityConfig;
import com.ikhsan.securepaywallet.common.config.WebMvcConfig;
import com.ikhsan.securepaywallet.user.controller.UserController;
import com.ikhsan.securepaywallet.user.entity.UserEntity;
import com.ikhsan.securepaywallet.user.service.UserService;
import com.ikhsan.securepaywallet.wallet.controller.WalletController;
import com.ikhsan.securepaywallet.wallet.entity.WalletEntity;
import com.ikhsan.securepaywallet.wallet.service.WalletService;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@DisplayName("Arsitektur: Penegakan Boundary Modular Monolith")
@AnalyzeClasses(packages = "com.ikhsan.securepaywallet", importOptions = {
        ImportOption.DoNotIncludeTests.class
})
public class ModuleBoundaryTest {

    // ── 1. Controller tidak boleh memanggil Repo langsung ────────────────────

    @ArchTest
    public static final ArchRule controllers_shouldNotDependOnRepositories =
            noClasses()
                    .that().resideInAPackage("..controller..")
                    .should().dependOnClassesThat().resideInAPackage("..repo..")
                    .because("Controller tidak boleh memanggil Repo secara langsung; semua akses data harus lewat service.");

    // ── 2. Akses antar-modul hanya melalui interface (I*) dan DTO ─────────────

    @ArchTest
    public static final ArchRule walletServiceImplementation_shouldNotBeAccessedByOtherModules =
            noClasses()
                    .that().resideOutsideOfPackage("..wallet..")
                    .should().dependOnClassesThat().haveFullyQualifiedName(WalletService.class.getName())
                    .because("Modul lain tidak boleh bergantung pada kelas konkret WalletService; gunakan interface IWallet.");

    @ArchTest
    public static final ArchRule userServiceImplementation_shouldNotBeAccessedByOtherModules =
            noClasses()
                    .that().resideOutsideOfPackage("..user..")
                    .should().dependOnClassesThat().haveFullyQualifiedName(UserService.class.getName())
                    .because("Modul lain tidak boleh bergantung pada kelas konkret UserService; gunakan interface IUser.");

    @ArchTest
    public static final ArchRule authServiceImplementation_shouldNotBeAccessedByOtherModules =
            noClasses()
                    .that().resideOutsideOfPackage("..auth..")
                    .should().dependOnClassesThat().haveFullyQualifiedName(AuthService.class.getName())
                    .because("Modul lain tidak boleh bergantung pada kelas konkret AuthService; gunakan interface IAuth.");

    @ArchTest
    public static final ArchRule sessionServiceImplementation_shouldNotBeAccessedByOtherModules =
            noClasses()
                    .that().resideOutsideOfPackage("..auth..")
                    .should().dependOnClassesThat().haveFullyQualifiedName(SessionService.class.getName())
                    .because("Modul lain tidak boleh bergantung pada kelas konkret SessionService; gunakan interface ISession.");

    // ── 3. Repo hanya diakses internal modul masing-masing ───────────────────

    @ArchTest
    public static final ArchRule walletRepository_shouldOnlyBeAccessedWithinWalletModule =
            classes()
                    .that().resideInAPackage("..wallet.repo..")
                    .should().onlyBeAccessed().byAnyPackage("..wallet..")
                    .because("WalletRepository hanya boleh diakses di dalam modul wallet.");

    @ArchTest
    public static final ArchRule sessionRepository_shouldOnlyBeAccessedWithinAuthModule =
            classes()
                    .that().haveSimpleName("SessionRepository")
                    .should().onlyBeAccessed().byAnyPackage("..auth..")
                    .because("SessionRepository hanya boleh diakses di dalam modul auth.");

    @ArchTest
    public static final ArchRule transactionRepository_shouldOnlyBeAccessedWithinTransactionModule =
            classes()
                    .that().haveSimpleName("TransactionRepository")
                    .should().onlyBeAccessed().byAnyPackage("..transaction..")
                    .because("TransactionRepository hanya boleh diakses di dalam modul transaction.");

    @ArchTest
    public static final ArchRule mutationRepository_shouldOnlyBeAccessedWithinMutationsModule =
            classes()
                    .that().haveSimpleName("MutationRepository")
                    .should().onlyBeAccessed().byAnyPackage("..mutations..")
                    .because("MutationRepository hanya boleh diakses di dalam modul mutations.");

    @ArchTest
    public static final ArchRule userRepository_shouldOnlyBeAccessedByUserModuleAndAuth =
            classes()
                    .that().resideInAPackage("..user.repo..")
                    .should().onlyBeAccessed().byAnyPackage(
                            "..user..",
                            // Pengecualian terdaftar: AuthService mengakses UserRepository untuk otentikasi/registrasi
                            "..auth.service..")
                    .because("UserRepository hanya boleh diakses modul user, dengan pengecualian AuthService.");

    // ── 4. Entity hanya diakses internal modul (dengan pengecualian JPA) ──────

    @ArchTest
    public static final ArchRule walletEntity_shouldOnlyBeAccessedByAuthorizedModules =
            classes()
                    .that().resideInAPackage("..wallet.entity..")
                    .should().onlyBeAccessed().byAnyPackage(
                            "..wallet..",
                            // Pengecualian JPA terdokumentasi: relasi JPA ke WalletEntity
                            "..user.entity..",
                            "..transaction..",
                            "..mutations..")
                    .because("WalletEntity hanya diakses internal wallet, dengan pengecualian relasi JPA.");

    @ArchTest
    public static final ArchRule sessionEntity_shouldOnlyBeAccessedWithinAuthModule =
            classes()
                    .that().resideInAPackage("..auth.session.entity..")
                    .and().haveSimpleName("SessionEntity")
                    .should().onlyBeAccessed().byAnyPackage("..auth..")
                    .because("SessionEntity hanya boleh diakses di dalam modul auth.");

    @ArchTest
    public static final ArchRule userEntity_shouldOnlyBeAccessedByAuthorizedModules =
            classes()
                    .that().resideInAPackage("..user.entity..")
                    .should().onlyBeAccessed().byAnyPackage(
                            "..user..",
                            // Pengecualian JPA terdokumentasi: relasi UserEntity di wallet dan session/auth
                            "..wallet..",
                            "..auth..")
                    .because("UserEntity adalah internal user; pengecualian relasi JPA di wallet dan auth.session.");

    // ── 5. Siklus dependensi antar-paket modul ────────────────────────────────

    @ArchTest
    public static final ArchRule noCyclicDependenciesBetweenFeatureModules =
            slices().matching("com.ikhsan.securepaywallet.(*)..")
                    .should().beFreeOfCycles()
                    // Pengecualian konfigurasi infrastruktur web/security di common:
                    .ignoreDependency(SecurityConfig.class, JwtAuthenticationFilter.class)
                    .ignoreDependency(WebMvcConfig.class, SessionActivityInterceptor.class)
                    // Pengecualian anotasi @SessionActivity lintas controller:
                    .ignoreDependency(UserController.class, SessionActivity.class)
                    .ignoreDependency(WalletController.class, SessionActivity.class)
                    // Pengecualian relasi JPA UserEntity <-> WalletEntity (utang teknis terdokumentasi):
                    .ignoreDependency(UserEntity.class, WalletEntity.class)
                    .because("Tidak boleh ada dependensi melingkar (cyclic dependencies) antar-modul di luar pengecualian yang dicatat.");
}
