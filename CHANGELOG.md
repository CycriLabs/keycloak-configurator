## [1.4.3](https://github.com/CycriLabs/keycloak-configurator/compare/1.4.2...1.4.3) (2025-07-09)


### Bug Fixes

* **configure:** flip name comparison for components to avoid NPEs ([b6e14d7](https://github.com/CycriLabs/keycloak-configurator/commit/b6e14d7c20d6c9ec4dcfdd7250ac737211f50fac))

## [1.4.2](https://github.com/CycriLabs/keycloak-configurator/compare/1.4.1...1.4.2) (2024-10-10)


### Bug Fixes

* compare env var with boolean 'strings' ([9a531b3](https://github.com/CycriLabs/keycloak-configurator/commit/9a531b3f3590278aee6a5531314e9c30baf76877))

## [1.4.1](https://github.com/CycriLabs/keycloak-configurator/compare/1.4.0...1.4.1) (2024-10-10)


### Bug Fixes

* disable native image build & artifacts creation ([69daf52](https://github.com/CycriLabs/keycloak-configurator/commit/69daf52f8213b45b24d950d5831128258fd6dcfb))

# [1.4.0](https://github.com/CycriLabs/keycloak-configurator/compare/1.3.1...1.4.0) (2024-07-30)


### Bug Fixes

* adapt entit export directory structure to import structure ([#59](https://github.com/CycriLabs/keycloak-configurator/issues/59)) ([0d6c6a8](https://github.com/CycriLabs/keycloak-configurator/commit/0d6c6a8f5d58322c2da796c64d49726a13dc978d))
* add a custom component class to resolve serialization issues with JSONB ([c9b3ad8](https://github.com/CycriLabs/keycloak-configurator/commit/c9b3ad8923bd580f938dc37cf7bc2022d4c4a54f))
* export client roles of each client if no specific requested ([#58](https://github.com/CycriLabs/keycloak-configurator/issues/58)) ([ad15e05](https://github.com/CycriLabs/keycloak-configurator/commit/ad15e05d8491637b255b09f79fb14e80802fac80))
* fix issues when exporting entities without given type ([5affde9](https://github.com/CycriLabs/keycloak-configurator/commit/5affde925b52a08d978c976a95c9c3160837bedd))
* handle user export errors gracefully ([#63](https://github.com/CycriLabs/keycloak-configurator/issues/63)) ([f720340](https://github.com/CycriLabs/keycloak-configurator/commit/f7203408fa9f0640eab5d055a1680bc3f6d93d0f))
* read in multiple realms in config store correctly ([#52](https://github.com/CycriLabs/keycloak-configurator/issues/52)) ([67adfd3](https://github.com/CycriLabs/keycloak-configurator/commit/67adfd312a086fa7c5c2862c8b7598681c6824a1))
* replace jsonb by jackson ([#54](https://github.com/CycriLabs/keycloak-configurator/issues/54)) ([bfdbbe3](https://github.com/CycriLabs/keycloak-configurator/commit/bfdbbe37ff3967217c75bb0f2563c4f7f7b561f5))


### Features

* add component exporter ([#51](https://github.com/CycriLabs/keycloak-configurator/issues/51)) ([ff098c4](https://github.com/CycriLabs/keycloak-configurator/commit/ff098c44d58a3eb588dc5e26a3c9176038d0fb7c))
* add importer for components ([#48](https://github.com/CycriLabs/keycloak-configurator/issues/48)) ([1da7209](https://github.com/CycriLabs/keycloak-configurator/commit/1da72090941196e02c6ca65c915d8db2ac5d06b2))
* execute exporters based on entity priority ([652a12d](https://github.com/CycriLabs/keycloak-configurator/commit/652a12d948dadaa818bda9b67e60af8b71c20566))
* format json on export ([#64](https://github.com/CycriLabs/keycloak-configurator/issues/64)) ([f2bd4b9](https://github.com/CycriLabs/keycloak-configurator/commit/f2bd4b9863da62a7e314bed67656b0b3bbfd2079))
* set default log level to INFO ([3213fb9](https://github.com/CycriLabs/keycloak-configurator/commit/3213fb919a6b9a4db8189732c3ed7b52f16eda03))

## [1.3.1](https://github.com/CycriLabs/keycloak-configurator/compare/1.3.0...1.3.1) (2024-04-04)


### Bug Fixes

* generate group hierarchy base on configuration files ([#45](https://github.com/CycriLabs/keycloak-configurator/issues/45)) ([bf28181](https://github.com/CycriLabs/keycloak-configurator/commit/bf28181de72cb00895b4dff4fabca86f2359ba51))
* import files in sorted order ([#44](https://github.com/CycriLabs/keycloak-configurator/issues/44)) ([10a4402](https://github.com/CycriLabs/keycloak-configurator/commit/10a440206077e844c549eecdd70ad1444441962c))

# [1.3.0](https://github.com/CycriLabs/keycloak-configurator/compare/1.2.0...1.3.0) (2024-04-03)


### Features

* bump quarkus to 3.9.2 ([6b98bee](https://github.com/CycriLabs/keycloak-configurator/commit/6b98bee92eeb594afba452f5faa05d76a6637b87))
* bump quarkus-velocity to 1.3.0 ([466ab02](https://github.com/CycriLabs/keycloak-configurator/commit/466ab0288b52bdc6158ac38b0c1ad2aa78c046ab))

# [1.2.0](https://github.com/CycriLabs/keycloak-configurator/compare/1.1.1...1.2.0) (2024-03-02)


### Features

* add support for defining client specific secret templates ([#40](https://github.com/CycriLabs/keycloak-configurator/issues/40)) ([2f9b428](https://github.com/CycriLabs/keycloak-configurator/commit/2f9b428785481afae59e415843322a177b5569dd))
* allow to export only specific client secrets ([#36](https://github.com/CycriLabs/keycloak-configurator/issues/36)) ([e4cf4ce](https://github.com/CycriLabs/keycloak-configurator/commit/e4cf4ce9cbf4d0faf2ae4ffffdc65cf3b260a731))
* bump quarkus to 3.7.1 ([1228ad8](https://github.com/CycriLabs/keycloak-configurator/commit/1228ad8bf89cf20bdca554cd442ce42bc7f106c6))
* bump quarkus to 3.7.3 ([6966c14](https://github.com/CycriLabs/keycloak-configurator/commit/6966c14d97100022e9a1cc453625bf9a652ad1cc))
* bump quarkus to 3.8.1 ([bb9474f](https://github.com/CycriLabs/keycloak-configurator/commit/bb9474f871feea0af574220b7a4ef1b76c14f822))
* bump quarkus-velocity to 1.1.0 ([886477d](https://github.com/CycriLabs/keycloak-configurator/commit/886477d21e35288cec421acbd9556845f2b8b854))
* bump quarkus-velocity to 1.2.0 ([881a14a](https://github.com/CycriLabs/keycloak-configurator/commit/881a14a89f9ae16523ad26d79f294aec20def01a))
* extend secret export variables by client object & clients map ([#39](https://github.com/CycriLabs/keycloak-configurator/issues/39)) ([8693552](https://github.com/CycriLabs/keycloak-configurator/commit/8693552c03107eb9b8e3c9517f9a4b59db512039))

## [1.1.1](https://github.com/CycriLabs/keycloak-configurator/compare/1.1.0...1.1.1) (2024-01-28)


### Bug Fixes

* register DTOs for reflection ([9e7d291](https://github.com/CycriLabs/keycloak-configurator/commit/9e7d2918ffeda21eef199f1e867911255f49aa59))

# [1.1.0](https://github.com/CycriLabs/keycloak-configurator/compare/1.0.0...1.1.0) (2024-01-27)


### Features

* adapt importers to new configuration dir structure ([#26](https://github.com/CycriLabs/keycloak-configurator/issues/26)) ([01e0f1a](https://github.com/CycriLabs/keycloak-configurator/commit/01e0f1afeb9ba442d71282ba5d3378e58d5df7ee))
* add cycrilabs banner ([1dc5dba](https://github.com/CycriLabs/keycloak-configurator/commit/1dc5dbaed13085797c9ad06624d660d514295887))
* add realm & client roles to user ([#25](https://github.com/CycriLabs/keycloak-configurator/issues/25)) ([43d29b3](https://github.com/CycriLabs/keycloak-configurator/commit/43d29b31777e88705852561ee45b3d450ba6f812))
* add service account realm role mapping ([#31](https://github.com/CycriLabs/keycloak-configurator/issues/31)) ([cb27e62](https://github.com/CycriLabs/keycloak-configurator/commit/cb27e6214cbbcd3f421a722a1c65a383c742e3bc))
* add service user client role mapping ([#30](https://github.com/CycriLabs/keycloak-configurator/issues/30)) ([cea475a](https://github.com/CycriLabs/keycloak-configurator/commit/cea475adf65b164f9f240d1976e9b1ee375d5f20))
* add support for single entity configuration ([#12](https://github.com/CycriLabs/keycloak-configurator/issues/12)) ([f9a15f5](https://github.com/CycriLabs/keycloak-configurator/commit/f9a15f5c02101b6787f6b1018edd37f693701b86))
* bump quarkus to 3.6.4 ([668e206](https://github.com/CycriLabs/keycloak-configurator/commit/668e20605e91b3fbb0743011678a166f26828612))
* bump quarkus to 3.6.6 ([1a5b5c9](https://github.com/CycriLabs/keycloak-configurator/commit/1a5b5c905be98ff32c990f34ba2c99fed95e453b))
* bump quarkus to 3.6.8 ([72a1a66](https://github.com/CycriLabs/keycloak-configurator/commit/72a1a669673590c4b616aee2d47c88fa121b7d33))
* bump quarkus-velocity to 1.0.0 ([341506e](https://github.com/CycriLabs/keycloak-configurator/commit/341506ea42e25b123a9413ae387383d4d0106b3e))

# 1.0.0 (2023-12-19)


### Bug Fixes

* add additional reflection classes ([#22](https://github.com/CycriLabs/keycloak-configurator/issues/22)) ([e86adde](https://github.com/CycriLabs/keycloak-configurator/commit/e86added3461b57fb514217e457b255032537b74))
* add reflection config for keycloak classes ([c9c0f1a](https://github.com/CycriLabs/keycloak-configurator/commit/c9c0f1a236cd8907040842c669cbed739736a577))
* remove default template ([#23](https://github.com/CycriLabs/keycloak-configurator/issues/23)) ([c11a626](https://github.com/CycriLabs/keycloak-configurator/commit/c11a626774c1499ad56d857e6ee70312b53e3886))


### Features

* add additional velocity variables ([#20](https://github.com/CycriLabs/keycloak-configurator/issues/20)) ([bfcb327](https://github.com/CycriLabs/keycloak-configurator/commit/bfcb3274a14eed5fddc242b38d32394772989228))
* add command to generate new secrets ([#14](https://github.com/CycriLabs/keycloak-configurator/issues/14)) ([e70ded7](https://github.com/CycriLabs/keycloak-configurator/commit/e70ded712630ae5a0fd0d490b7f60e71d2ea3c57))
* add configuration command ([#1](https://github.com/CycriLabs/keycloak-configurator/issues/1)) ([a0f8a5f](https://github.com/CycriLabs/keycloak-configurator/commit/a0f8a5f29c753b9e69055582919c06c0030f651b))
* add entity exporter command ([#13](https://github.com/CycriLabs/keycloak-configurator/issues/13)) ([df43882](https://github.com/CycriLabs/keycloak-configurator/commit/df43882e7a4bf4f57aa0bef0439e0eb882b7297d))
* add quarkus velocity extension ([#22](https://github.com/CycriLabs/keycloak-configurator/issues/22)) ([bd825f3](https://github.com/CycriLabs/keycloak-configurator/commit/bd825f3be1833e7406a43e258a256bd9f30930ba))
* add release workflow ([cee5638](https://github.com/CycriLabs/keycloak-configurator/commit/cee5638a785d112e96b5a1dabbc4a7964596db51))
* add secret export command ([#2](https://github.com/CycriLabs/keycloak-configurator/issues/2)) ([174240f](https://github.com/CycriLabs/keycloak-configurator/commit/174240f0a93bedc71de4560eb5b8c881f2a0d618))
* bump lombok to 1.18.30 ([f6e56ad](https://github.com/CycriLabs/keycloak-configurator/commit/f6e56adda27e6ff1299b6416bc6f6a4f910902b1))
* bump quarkus to 3.4.1 ([66dc41c](https://github.com/CycriLabs/keycloak-configurator/commit/66dc41c6ca61f8f4676cf4e3d94f34cb6905d344))
* bump quarkus to 3.4.2 ([c4cede2](https://github.com/CycriLabs/keycloak-configurator/commit/c4cede298da8ed3310bc17a2e9d6219ced96779c))
* bump quarkus to 3.5.0 ([d7950c7](https://github.com/CycriLabs/keycloak-configurator/commit/d7950c7c88ccec857131abe1d257a99689466958))
* bump quarkus to 3.6.3 ([b86e434](https://github.com/CycriLabs/keycloak-configurator/commit/b86e434f5ab247ca265245fc161b269e955de104))
* enable interactive password input ([#5](https://github.com/CycriLabs/keycloak-configurator/issues/5)) ([0e91ec4](https://github.com/CycriLabs/keycloak-configurator/commit/0e91ec4d3e9bb4cdf10e3e900325b33479725fc5))
* initialize repository ([4859734](https://github.com/CycriLabs/keycloak-configurator/commit/48597343e025430d032d6a3bd1cf976f6f6363e8))
* switch source/target to java 21 ([0946040](https://github.com/CycriLabs/keycloak-configurator/commit/094604012c29a51a28989d614421f8e4ec462694))
