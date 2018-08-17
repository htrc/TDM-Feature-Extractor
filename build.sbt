import com.typesafe.sbt.{GitBranchPrompt, GitVersioning}

showCurrentGitBranch

git.useGitDescribe := true

lazy val commonSettings = Seq(
  organization := "tdm",
  organizationName := "Text and Data Mining (TDM) initiative involving HathiTrust/HTRC, JSTOR, and Portico",
  scalaVersion := "2.12.6",
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-language:postfixOps",
    "-language:implicitConversions"
  ),
  resolvers ++= Seq(
    "HTRC Repository" at "http://nexus.htrc.illinois.edu/content/groups/public",
    Resolver.mavenLocal
  ),
  publishTo := {
    val nexus = "https://nexus.htrc.illinois.edu/"
    if (isSnapshot.value)
      Some("HTRC Snapshots Repository" at nexus + "content/repositories/snapshots")
    else
      Some("HTRC Releases Repository"  at nexus + "content/repositories/releases")
  },
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials" / "nexus.htrc.illinois.edu"),
  packageOptions in (Compile, packageBin) += Package.ManifestAttributes(
    ("Git-Sha", git.gitHeadCommit.value.getOrElse("N/A")),
    ("Git-Branch", git.gitCurrentBranch.value),
    ("Git-Version", git.gitDescribedVersion.value.getOrElse("N/A")),
    ("Git-Dirty", git.gitUncommittedChanges.value.toString),
    ("Build-Date", new java.util.Date().toString)
  ),
//  wartremoverErrors ++= Warts.unsafe.diff(Seq(
//    Wart.DefaultArguments,
//    Wart.NonUnitStatements,
//    Wart.Any,
//    Wart.TryPartial
//  )),
  // force to run 'test' before 'package' and 'publish' tasks
  publish := (publish dependsOn Test / test).value,
  Keys.`package` := (Compile / Keys.`package` dependsOn Test / test).value
)

lazy val `feature-extractor` = (project in file(".")).
  enablePlugins(GitVersioning, GitBranchPrompt).
  settings(commonSettings).
  settings(
    name := "feature-extractor",
    description := "Extracts a set of features (such as ngram counts, POS tags, etc.) from a " +
      "corpus for aiding in conducting 'distant-reading' (aka non-consumptive) research",
    licenses += "Apache2" -> url("http://www.apache.org/licenses/LICENSE-2.0"),
    libraryDependencies ++= Seq(
      "org.hathitrust.htrc"           %% "data-model"           % "1.3.1",
      "org.hathitrust.htrc"           %% "scala-utils"          % "2.6",
      "edu.stanford.nlp"              %  "stanford-corenlp"     % "3.9.1",
      "edu.stanford.nlp"              %  "stanford-corenlp"     % "3.9.1"
        classifier "models"
        classifier "models-arabic"
        classifier "models-chinese"
        classifier "models-english"
        classifier "models-french"
        classifier "models-german"
        classifier "models-spanish",
      "com.optimaize.languagedetector" % "language-detector"    % "0.6",
      "commons-codec"                 %  "commons-codec"        % "1.11",
      "org.scalacheck"                %% "scalacheck"           % "1.14.0"      % Test,
      "org.scalatest"                 %% "scalatest"            % "3.0.5"       % Test
    ),
    crossScalaVersions := Seq("2.12.6", "2.11.12")
  )
