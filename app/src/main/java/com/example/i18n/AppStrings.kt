package com.example.i18n

object AppStrings {

    fun appTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إحمي روحك"
        AppLanguage.FRENCH -> "Protect Yourself"
        AppLanguage.ENGLISH -> "Protect Yourself"
    }

    fun appSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "توثيق الأدلة للمواطنين • فهم الحقوق • نشر مسؤول"
        AppLanguage.FRENCH -> "Préservation de preuves citoyennes • Comprendre ses droits • Partage responsable"
        AppLanguage.ENGLISH -> "Preserve information. Understand your rights. Share responsibly."
    }

    // Navigation
    fun navHome(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الرئيسية"
        AppLanguage.FRENCH -> "Accueil"
        AppLanguage.ENGLISH -> "Home"
    }

    fun navPublicFeed(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "التسجيلات العامة"
        AppLanguage.FRENCH -> "Public"
        AppLanguage.ENGLISH -> "Public"
    }

    fun navRecord(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تسجيل"
        AppLanguage.FRENCH -> "Enregistrer"
        AppLanguage.ENGLISH -> "Record"
    }

    fun navMyRecordings(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تسجيلاتي"
        AppLanguage.FRENCH -> "Mes audios"
        AppLanguage.ENGLISH -> "My Audios"
    }

    fun navAccount(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "حسابي"
        AppLanguage.FRENCH -> "Profil"
        AppLanguage.ENGLISH -> "Account"
    }

    // Legal Gate
    fun legalGateTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "معلومات قانونية هامة"
        AppLanguage.FRENCH -> "Informations Juridiques Importantes"
        AppLanguage.ENGLISH -> "Important Legal Information"
    }

    fun legalGateSummary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> """
            منصة "إحمي روحك" هي أداة لمساعدة المواطنين على توثيق وحفظ الأدلة الصوتية المتعلقة بالتعاملات مع الإدارات العمومية، والمصالح الحكومية، والأمن، والأماكن العامة في تونس.
            
            • كل تسجيل يتم نشره يصبح علنياً ومتاحاً للعموم (لا يوجد وضع تسجيل خاص بعد النشر).
            • المستخدم يتحمل المسؤولية القانونية الكاملة عن المحتوى الذي يقوم بنشره.
            • ينص القانون التونسي (القانون الأساسي عدد 63 لسنة 2004 ومجلة الإجراءات الجزائية ومجلة الاتصالات) على حماية المعطيات الشخصية والحياة الخاصة وحرمة المراسلات.
            • نشر هويات أو أصوات أشخاص يمكن التعرف عليهم قد يترتب عنه تتبعات قانونية إذا تم بصفة غير مشروعة.
            • يُمنع منعاً باتاً نشر الاتهامات الباطلة، والتشهير، والسب، والتهديد، وإفشاء الأسرار الشخصية، والتحريض.
            • التطبيق لا يُثبت ولا يبتّ في صحة الإدعاءات قانونياً؛ الحكم للبراءة أو الإدانة من اختصاص القضاء حصراً.
            • المعلومات الواردة في هذا التطبيق هي للتوعية العامة وليست استشارة قانونية فردية بديلة عن محامٍ.
        """.trimIndent()
        AppLanguage.FRENCH -> """
            La plateforme "Protect Yourself" (إحمي روحك) est un outil citoyen destiné à préserver et documenter des éléments audios lors d'interactions avec les services publics, l'administration, les forces de l'ordre ou dans l'espace public en Tunisie.
            
            • Tout enregistrement publié devient public et accessible à tous (aucun mode privé après publication).
            • L'utilisateur est légalement et exclusivement responsable des contenus qu'il enregistre et publie.
            • La loi tunisienne (Loi organique n° 2004-63, Code pénal, Code des télécommunications) protège la vie privée et les données personnelles.
            • La publication d'éléments identifiant des tiers peut engager la responsabilité pénale et civile de son auteur.
            • Sont strictement interdits : fausses accusations, diffamation, menaces, doxxing ou atteintes malveillantes.
            • La plateforme ne statue pas sur la culpabilité ou la véracité juridique d'une infraction alléguée.
            • Ces informations constituent des repères généraux et ne sauraient remplacer l'avis d'un avocat tunisien qualifié.
        """.trimIndent()
        AppLanguage.ENGLISH -> """
            "Protect Yourself" (إحمي روحك) is a citizen evidence platform to document interactions with public officials, administrative services, police, and in public situations in Tunisia.
            
            • All published recordings are public and permanently accessible (there is no private mode once published).
            • Users bear full legal responsibility for any recordings or metadata they choose to submit.
            • Tunisian law (Organic Law 2004-63, Penal Code, Telecommunications Code) protects privacy and personal data.
            • Publishing identifiable individuals may lead to legal consequences under applicable law.
            • Fabricated accusations, defamation, threats, doxxing, and malicious publications are strictly prohibited.
            • The platform does not determine whether an alleged violation is legally proven; only competent courts have that authority.
            • Information provided in this application is for general guidance and is not a substitute for legal advice.
        """.trimIndent()
    }

    fun readTunisianLawBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "قراءة المرجع القانوني التونسي"
        AppLanguage.FRENCH -> "Consulter le Référentiel Juridique Tunisien"
        AppLanguage.ENGLISH -> "Read Tunisian Legal Information"
    }

    fun legalAcknowledgement(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "لقد قرأت هذه المعلومات القانونية وفهمت التزاماتي ومسؤوليتي قبل استخدام التطبيق."
        AppLanguage.FRENCH -> "J'ai lu et compris ces informations juridiques ainsi que ma responsabilité."
        AppLanguage.ENGLISH -> "I have read and understood this legal information and my responsibility."
    }

    fun enterAppBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الدخول إلى التطبيق"
        AppLanguage.FRENCH -> "Accéder à l'application"
        AppLanguage.ENGLISH -> "Enter Application"
    }

    // Public Warning Dialog / Sheet
    fun publicUploadTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "نشر علني — تحذير هام"
        AppLanguage.FRENCH -> "Publication Publique"
        AppLanguage.ENGLISH -> "Public Upload"
    }

    fun publicUploadWarning(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "سيكون هذا التسجيل متاحاً للعموم بشكل دائم. تأكد من إدراكك الكامل للآثار القانونية وحماية المعطيات الشخصية قبل تأكيد النشر."
        AppLanguage.FRENCH -> "Cet enregistrement sera accessible publiquement. Assurez-vous d'avoir compris les conséquences juridiques et relatives à la vie privée avant de publier."
        AppLanguage.ENGLISH -> "This recording will be publicly accessible. Make sure you understand the privacy and legal implications before publishing."
    }

    fun cancelBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إلغاء"
        AppLanguage.FRENCH -> "Annuler"
        AppLanguage.ENGLISH -> "Cancel"
    }

    fun publishPubliclyBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تأكيد النشر للعموم"
        AppLanguage.FRENCH -> "Publier Publiquement"
        AppLanguage.ENGLISH -> "Publish Publicly"
    }

    // Claim vs Fact distinction
    fun labelApplicationFact(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "بيانات تقنية مثبتة للتطبيق"
        AppLanguage.FRENCH -> "Fait Technique de l'Application"
        AppLanguage.ENGLISH -> "Application Fact"
    }

    fun labelUserClaim(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تصريح وادعاء الناشر"
        AppLanguage.FRENCH -> "Déclaration de l'Utilisateur"
        AppLanguage.ENGLISH -> "User Claim"
    }

    fun labelLegallyEstablishedFact(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "واقعة مثبتة قضائياً"
        AppLanguage.FRENCH -> "Fait Juridiquement Établi"
        AppLanguage.ENGLISH -> "Legally Established Fact"
    }

    fun claimDisclaimer(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تنبيه: الأوصاف والتصنيفات أعلاه صادرة عن الناشر ولا تُعد إدانة قانونية أو حكماً قضائياً مسبقاً."
        AppLanguage.FRENCH -> "Note : Les descriptions ci-dessus proviennent de l'auteur et ne constituent pas un jugement ou une vérité juridique."
        AppLanguage.ENGLISH -> "Note: Descriptions above represent claims by the uploader and do not constitute a legal determination."
    }

    // Categories
    fun categoryPolice(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تعامل مع الأمن / الشرطة"
        AppLanguage.FRENCH -> "Interaction policière"
        AppLanguage.ENGLISH -> "Police interaction"
    }

    fun categoryGovService(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "خدمة حكومية / بلدية"
        AppLanguage.FRENCH -> "Service gouvernemental"
        AppLanguage.ENGLISH -> "Government service"
    }

    fun categoryAdministration(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إدارة ومرفق عمومي"
        AppLanguage.FRENCH -> "Administration publique"
        AppLanguage.ENGLISH -> "Public administration"
    }

    fun categoryTransport(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "نقل عمومي ومحطات"
        AppLanguage.FRENCH -> "Transport public"
        AppLanguage.ENGLISH -> "Public transport"
    }

    fun categoryPublicSpace(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "حادثة في فضاء عام"
        AppLanguage.FRENCH -> "Incident en espace public"
        AppLanguage.ENGLISH -> "Public-space incident"
    }

    fun categoryOther(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "أخرى"
        AppLanguage.FRENCH -> "Autre"
        AppLanguage.ENGLISH -> "Other"
    }

    // Recording Flow
    fun recordingStudioTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "استوديو التسجيل والتوثيق"
        AppLanguage.FRENCH -> "Studio d'Enregistrement"
        AppLanguage.ENGLISH -> "Evidence Recording Studio"
    }

    fun tapToRecord(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "اضغط لبدء التسجيل الفوري"
        AppLanguage.FRENCH -> "Appuyez pour enregistrer"
        AppLanguage.ENGLISH -> "Tap to start recording"
    }

    fun recordingActive(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "جاري التسجيل الصوتي الحقيقي..."
        AppLanguage.FRENCH -> "Enregistrement en cours..."
        AppLanguage.ENGLISH -> "Recording active..."
    }

    fun micPermissionNeeded(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "يرجى منح إذن الميكروفون للتمكن من تسجيل الصوت."
        AppLanguage.FRENCH -> "Permission microphone requise pour enregistrer."
        AppLanguage.ENGLISH -> "Microphone permission is required to record audio."
    }

    fun grantPermission(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "منح إذن الميكروفون"
        AppLanguage.FRENCH -> "Autoriser le micro"
        AppLanguage.ENGLISH -> "Grant Microphone Permission"
    }

    fun orUploadFile(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "أو استيراد ملف صوتي من الجهاز"
        AppLanguage.FRENCH -> "Ou importer un fichier audio existant"
        AppLanguage.ENGLISH -> "Or import an audio file"
    }

    fun pauseRecord(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إيقاف مؤقت"
        AppLanguage.FRENCH -> "Pause"
        AppLanguage.ENGLISH -> "Pause"
    }

    fun resumeRecord(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "متابعة"
        AppLanguage.FRENCH -> "Reprendre"
        AppLanguage.ENGLISH -> "Resume"
    }

    fun stopRecord(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إنهاء وحفظ"
        AppLanguage.FRENCH -> "Terminer"
        AppLanguage.ENGLISH -> "Stop & Review"
    }

    fun hashExplanation(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "البصمة المشفرة (SHA-256) تساعد في التحقق من عدم المساس بالملف أو تعديله بعد الرفع. لا تضمن البصمة صحة أو دقة المحتوى قبل التسجيل."
        AppLanguage.FRENCH -> "L'empreinte cryptographique (SHA-256) permet de certifier que le fichier n'a pas été modifié après upload. Elle ne certifie pas l'authenticité des faits enregistrés."
        AppLanguage.ENGLISH -> "The cryptographic integrity hash (SHA-256) helps detect whether the stored file was altered after upload. It does not prove the recording was unedited before upload."
    }

    // Reports
    fun reportRecording(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إبلاغ عن محتوى"
        AppLanguage.FRENCH -> "Signaler cet audio"
        AppLanguage.ENGLISH -> "Report recording"
    }

    fun reportSubmitted(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تم استلام البلاغ وسيتم مراجعته وفق سياسة المنصة والقانون."
        AppLanguage.FRENCH -> "Signalement transmis. Il sera analysé conformément à nos règles."
        AppLanguage.ENGLISH -> "Report submitted. It will be reviewed in accordance with policy and law."
    }

    // Privacy & Account
    fun privacyNote(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "لحماية خصوصيتك: لا نطلب اسمك الحقيقي أو بطاقة تعريفك الوطنية. لا تضع بياناتك الشخصية داخل الأوصاف إذا كنت لا ترغب بنشرها للعموم."
        AppLanguage.FRENCH -> "Protection des données : nous n'exigeons ni nom d'état civil ni numéro de CIN. N'incluez pas d'éléments personnels dans vos descriptions."
        AppLanguage.ENGLISH -> "Privacy policy: We do not require your real name, government ID, or phone number. Do not submit personal details you do not want public."
    }
}
