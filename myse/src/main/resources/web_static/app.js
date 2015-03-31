(function () {
	var app = angular.module('myapp', []);

	app.controller('MyappController', function () {
		this.products = gems;
	});

	var gems = [{
			name: 'Louis',
			price: 1.30,
			ok: false,
			hide: false
		},
		{
			name: 'Charles',
			price: 40,
			ok: true
		},
		{
			name: 'Alice',
			price: 0,
			ok: true,
			hide: true
		}
	];
})();
