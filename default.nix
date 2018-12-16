{ nixpkgs ? import ./nix/nixpkgs.nix {} }:
{
    uphubd = nixpkgs.callPackage ./uphubd { };
}
