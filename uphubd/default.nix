{ stdenv, sbt }:
stdenv.mkDerivation {
    name = "uphubd";
    src = ./.;
    buildInputs = [
        sbt
    ];
    buildPhase = ''
        false
    '';
}
