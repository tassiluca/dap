use bindgen;

fn main() {
    // Tell cargo to rerun the build script if the C header changes
    println!("cargo:rerun-if-changed=../c/foo/foo.h");

    // Build the bindings for the C header
    let bindings = bindgen::Builder::default()
        .header("../c/foo/foo.h") // Specify the header file
        .generate()
        .expect("Unable to generate bindings");

    // Write the bindings to a Rust source file
    bindings
        .write_to_file("src/foo_bindings.rs")
        .expect("Couldn't write bindings!");
}
